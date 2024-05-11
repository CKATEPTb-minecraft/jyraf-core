package dev.ckateptb.minecraft.jyraf.collider.geometry;

import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.collider.Colliders;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public class RayTraceCollider implements Collider<RayTraceCollider> {
    private final Location start;
    private final ImmutableVector direction;
    private final ImmutableVector size;
    private final OrientedBoundingBoxCollider obb;

    public RayTraceCollider(Location start, Vector direction, double width, double height, double distance) {
        this(start, direction, new ImmutableVector(width, height, distance));
    }

    public RayTraceCollider(Location start, Vector direction, Vector size) {
        this.start = start.clone();
        this.direction = ImmutableVector.of(direction).normalize();
        this.size = ImmutableVector.of(size);
        double halfDistance = size.getZ() / 2;
        Location center = start.clone().add(this.direction.multiply(halfDistance));
        this.obb = Colliders.obb(center, this.size.setZ(halfDistance), this.direction.directionToEulerAngle());
    }

    @Override
    public @NotNull RayTraceCollider at(@NotNull Location start) {
        return new RayTraceCollider(start, this.direction, this.size);
    }

    @Override
    public @NotNull RayTraceCollider grow(Vector vector) {
        return new RayTraceCollider(this.start, this.direction, this.size.add(vector));
    }

    @Override
    public @Nullable RayTraceCollider scale(double amount) {
        return new RayTraceCollider(this.start, this.direction, this.size.multiply(amount));
    }

    @Override
    public @NotNull ImmutableVector getHalfExtents() {
        return this.obb.getHalfExtents();
    }

    @Override
    public boolean contains(@NotNull Vector vector) {
        return this.obb.contains(vector);
    }

    @Override
    public @NotNull Location getLocation() {
        return this.start.clone();
    }

    @Override
    public LazyLoader<Collection<ImmutableVector>> getDraw() {
        return this.obb.getDraw();
    }

    @Override
    public @NotNull Flux<Block> findBlocks() {
        return Collider.super.findBlocks().sort((o1, o2) -> {
            Location first = o1.getLocation();
            Location second = o2.getLocation();
            return Double.compare(first.distanceSquared(this.start), second.distanceSquared(this.start));
        });
    }

    @Override
    public @NotNull Flux<Entity> findEntities() {
        return Collider.super.findEntities().sort((o1, o2) -> {
            Location first = o1.getLocation();
            Location second = o2.getLocation();
            return Double.compare(first.distanceSquared(this.start), second.distanceSquared(this.start));
        });
    }

    public double getDistance() {
        return this.size.getZ();
    }

    public double getWidth() {
        return this.size.getX();
    }

    public double getHeight() {
        return this.size.getY();
    }

    public RayTraceBlock block() {
        return new RayTraceBlock();
    }

    public RayTraceEntity entity() {
        return new RayTraceEntity();
    }

    public RayTracePosition position() {
        return new RayTracePosition();
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public class RayTracePosition {
        private RayTraceEntity entity;
        private RayTraceBlock block;

        public Mono<ImmutableVector> find() {
            Mono<ImmutableVector> destination = Mono.empty();
            ImmutableVector vector = ImmutableVector.of(start);
            ImmutableVector position = vector.add(direction.multiply(getDistance()));
            if (block != null) {
                destination = Mono.justOrEmpty(block.find())
                        .mapNotNull(block -> {
                            double distance = vector.distance(block.getLocation().toCenterLocation().toVector()) - 0.5;
                            return vector.add(direction.multiply(distance));
                        });
            }
            if (entity != null) {
                destination = destination
                        .switchIfEmpty(Mono.just(position))
                        .zipWhen(pos -> entity.find().mapNotNull(entity -> ImmutableVector.of(entity.getLocation())
                                .add(0, entity.getHeight() / 2, 0)))
                        .map(objects -> {
                            ImmutableVector t1 = objects.getT1();
                            ImmutableVector t2 = objects.getT2();
                            if (vector.distanceSquared(t1) <= vector.distanceSquared(t2)) {
                                return t1;
                            }
                            return t2;
                        });
            }
            return destination.switchIfEmpty(Mono.just(position));
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public class RayTraceBlock {
        private boolean liquid;
        private boolean passable;
        private boolean obstacles;
        private Predicate<Block> filter;

        public Optional<Block> find() {
            double size = FastMath.max(RayTraceCollider.this.getWidth(), RayTraceCollider.this.getHeight());
            int distance = FastMath.toIntExact(FastMath.round(RayTraceCollider.this.getDistance()) + 1);
            BlockIterator it = new BlockIterator(getWorld(), start.toVector(), direction, size, distance);
            while (it.hasNext()) {
                Block block = it.next();
                if (block.isPassable()) {
                    if (block.isLiquid()) {
                        if (!this.liquid) {
                            continue;
                        }
                    } else if (!this.passable) {
                        continue;
                    }
                }
                if (this.filter == null || this.filter.test(block)) {
                    return Optional.of(block);
                }
                if (!this.obstacles && !block.isPassable()) {
                    break;
                }
            }
            return Optional.empty();
        }
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public class RayTraceEntity {
        private Predicate<Entity> filter;

        public Mono<Entity> find() {
            return findEntities().filter(entity -> filter == null || filter.test(entity)).next();
        }
    }
}