package dev.ckateptb.minecraft.jyraf.colider.geometry;

import com.google.common.base.Objects;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.colider.Collider;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Sinks;
import dev.ckateptb.minecraft.jyraf.internal.reactor.util.function.Tuple3;
import dev.ckateptb.minecraft.jyraf.internal.reactor.util.function.Tuples;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.Getter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

@Getter
public class AxisAlignedBoundingBoxCollider implements Collider {
    public static final CachedReference<Mono<WorldService>> WORLD_SERVICE_CACHED_REFERENCE =
            new CachedReference<>(() -> IoC.getBean(WorldService.class).orElseGet(Mono::empty));

    protected final World world;
    protected final ImmutableVector min;
    protected final ImmutableVector max;

    public AxisAlignedBoundingBoxCollider(World world, ImmutableVector min, ImmutableVector max) {
        this.world = world;
        this.min = min.min(max);
        this.max = max.max(min);
    }

    @Override
    public AxisAlignedBoundingBoxCollider at(Vector center) {
        ImmutableVector halfExtents = this.getHalfExtents();
        ImmutableVector immutableCenter = ImmutableVector.of(center);
        return new AxisAlignedBoundingBoxCollider(world, immutableCenter.add(halfExtents.negative()), immutableCenter.add(halfExtents));
    }

    @Override
    public AxisAlignedBoundingBoxCollider grow(Vector vector) {
        return new AxisAlignedBoundingBoxCollider(world, min.subtract(vector), max.add(vector));
    }

    @Override
    public AxisAlignedBoundingBoxCollider scale(double multiplier) {
        return this.scale(multiplier, multiplier, multiplier);
    }

    public AxisAlignedBoundingBoxCollider scale(double multiplierX, double multiplierY, double multiplierZ) {
        ImmutableVector halfExtents = this.getHalfExtents();
        ImmutableVector newExtents = new ImmutableVector(halfExtents.getX() * multiplierX,
                halfExtents.getY() * multiplierY,
                halfExtents.getZ() * multiplierZ);
        ImmutableVector diff = newExtents.subtract(halfExtents);
        return new AxisAlignedBoundingBoxCollider(world, min.subtract(diff), max.add(diff));
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return max.subtract(min).multiply(0.5).abs();
    }

    @Override
    public ImmutableVector getCenter() {
        return min.add(max.subtract(min).multiply(0.5));
    }

    @Override
    public boolean contains(Vector vector) {
        return vector.isInAABB(min, max);
    }

    private boolean intersects(AxisAlignedBoundingBoxCollider first, AxisAlignedBoundingBoxCollider second) {
        return first.min.getX() <= second.max.getX()
                && first.max.getX() >= second.min.getX()
                && first.min.getY() <= second.max.getY()
                && first.max.getY() >= second.min.getY()
                && first.min.getZ() <= second.max.getZ()
                && first.max.getZ() >= second.min.getZ();
    }

    @Override
    public boolean intersects(Collider other) {
        World otherWorld = other.getWorld();
        if (!otherWorld.equals(world)) return false;
        if (other instanceof AxisAlignedBoundingBoxCollider aabb) {
            return this.intersects(aabb, this) || this.intersects(this, aabb);
        }
        if (other instanceof SphereBoundingBoxCollider sphere) {
            return sphere.intersects(this);
        }
        if (other instanceof OrientedBoundingBoxCollider obb) {
            return obb.intersects(this);
        }
        if (other instanceof RayTraceCollider ray) {
            return ray.intersects(this);
        }
        return false;
    }

    @Override
    public AxisAlignedBoundingBoxCollider affectEntities(Consumer<Flux<Entity>> consumer) {
        ImmutableVector center = this.getCenter();
        ImmutableVector vector = min.max(max).subtract(center);
        consumer.accept(Mono.defer(() -> Mono.just(center.toLocation(world)))
                .flatMapMany(location -> WORLD_SERVICE_CACHED_REFERENCE.get().orElseGet(Mono::empty)
                        .flatMap(worldService -> worldService.getWorld(world.getUID()))
                        .flatMapMany(worldRepository -> worldRepository.getNearbyEntities(location, vector.maxComponent())))
                .filter(entity -> this.intersects(Colliders.aabb(entity))));
        return this;
    }

    @Override
    public AxisAlignedBoundingBoxCollider affectBlocks(Consumer<Flux<Block>> consumer) {
        this.affectLocations(flux ->
                consumer.accept(flux
                        .map(Location::getBlock)
                        .filter(block -> {
                            AxisAlignedBoundingBoxCollider aabb = Colliders.aabb(block);
                            return aabb.intersects(this) || this.intersects(aabb);
                        })));
        return this;
    }

    @Override
    public AxisAlignedBoundingBoxCollider affectLocations(Consumer<Flux<Location>> consumer) {
        ImmutableVector position = this.getCenter();
        double maxExtent = getHalfExtents().maxComponent();
        int radius = (int) (FastMath.ceil(maxExtent) + 1);
        double originX = position.getX();
        double originY = position.getY();
        double originZ = position.getZ();
        Sinks.Many<Tuple3<Double, Double, Double>> locations = Sinks.many().unicast().onBackpressureBuffer();
        Flux<Tuple3<Double, Double, Double>> flux = locations.asFlux();
        consumer.accept(flux
                .map(tuple -> new ImmutableVector(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                .map(vector -> vector.toLocation(world).toCenterLocation())
                .filter(location -> {
                    Collider aabb = Colliders.BLOCK.apply(world).at(location);
                    return aabb.intersects(this) || this.intersects(aabb);
                }));
        for (double x = originX - radius; x <= originX + radius; x++) {
            for (double y = originY - radius; y <= originY + radius; y++) {
                for (double z = originZ - radius; z <= originZ + radius; z++) {
                    locations.tryEmitNext(Tuples.of(x, y, z));
                }
            }
        }
        locations.tryEmitComplete();
        return this;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AxisAlignedBoundingBoxCollider that)) return false;
        return Objects.equal(world, that.world) && Objects.equal(min, that.min) && Objects.equal(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(world, min, max);
    }

    @Override
    public String toString() {
        return "AxisAlignedBoundingBoxCollider{" +
                "world=" + world.getName() +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}