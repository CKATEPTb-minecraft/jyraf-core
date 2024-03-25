package dev.ckateptb.minecraft.jyraf.colider.geometry;

import dev.ckateptb.minecraft.jyraf.colider.Collider;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.repository.entity.EntityRepository;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RayTraceCollider implements Collider {
    @Getter
    protected final World world;
    @Getter
    private final ImmutableVector center;
    private final ImmutableVector direction;
    private final double distance;
    private final double size;
    private final OrientedBoundingBoxCollider orientedBoundingBoxCollider;

    public RayTraceCollider(World world, ImmutableVector center, ImmutableVector direction, double distance, double size) {
        this.world = world;
        this.center = center;
        this.direction = direction.normalize();
        this.distance = distance;
        this.size = size;
        this.orientedBoundingBoxCollider = this.toOrientedBoundingBox();
    }

    @Override
    public RayTraceCollider at(Vector center) {
        return new RayTraceCollider(this.world, ImmutableVector.of(center), this.direction, this.distance, this.size);
    }

    @Override
    public RayTraceCollider grow(Vector vector) {
        return new RayTraceCollider(this.world, this.center, this.direction, this.distance + vector.getZ(),
                this.size + FastMath.max(vector.getX(), vector.getY()));
    }

    @Override
    public RayTraceCollider scale(double amount) {
        return null;
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return this.orientedBoundingBoxCollider.getHalfExtents();
    }

    @Override
    public <RT extends Collider> boolean intersects(RT collider) {
        return this.orientedBoundingBoxCollider.intersects(collider);
    }

    @Override
    public boolean contains(Vector vector) {
        return this.orientedBoundingBoxCollider.contains(vector);
    }

    @Override
    public RayTraceCollider affectEntities(Consumer<Flux<Entity>> consumer) {
        this.orientedBoundingBoxCollider.affectEntities(consumer);
        return this;
    }

    @Override
    public RayTraceCollider affectBlocks(Consumer<Flux<Block>> consumer) {
        this.orientedBoundingBoxCollider.affectBlocks(consumer);
        return this;
    }

    @Override
    public RayTraceCollider affectLocations(Consumer<Flux<Location>> consumer) {
        this.orientedBoundingBoxCollider.affectLocations(consumer);
        return this;
    }

    private OrientedBoundingBoxCollider toOrientedBoundingBox() {
        ImmutableVector immutableVector = new ImmutableVector(this.size, this.size, this.distance);
        final double _2PI = 2 * FastMath.PI;
        final double x = this.direction.getX();
        final double z = this.direction.getZ();
        float pitch, yaw;
        if (x == 0 && z == 0) {
            pitch = this.direction.getY() > 0 ? -90 : 90;
            yaw = 0;
        } else {
            double theta = FastMath.atan2(-x, z);
            yaw = (float) FastMath.toDegrees((theta + _2PI) % _2PI);
            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = FastMath.sqrt(x2 + z2);
            pitch = (float) FastMath.toDegrees(FastMath.atan(-this.direction.getY() / xz));
        }
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        return Colliders.obb(this.world, this.center.add(this.direction.multiply(this.distance)),
                immutableVector, eulerAngle);
    }

    public Mono<Map.Entry<Block, BlockFace>> getFirstBlock(boolean ignoreLiquids, boolean ignorePassable) {
        return this.getFirstBlockOptional(ignoreLiquids, ignorePassable)
                .map(Mono::just)
                .orElseGet(Mono::empty);
    }

    public Optional<Map.Entry<Block, BlockFace>> getFirstBlockOptional(boolean ignoreLiquids, boolean ignorePassable) {
        RayTraceResult traceResult = this.world.rayTraceBlocks(this.center.toLocation(this.world), this.direction,
                this.distance, ignoreLiquids ? FluidCollisionMode.NEVER : FluidCollisionMode.ALWAYS, ignorePassable);
        if (traceResult == null) return Optional.empty();
        Block block = traceResult.getHitBlock();
        BlockFace blockFace = traceResult.getHitBlockFace();
        return block == null || blockFace == null ? Optional.empty() : Optional.of(Map.entry(block, blockFace));
    }

    public Mono<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable, Predicate<Block> filter) {
        return this.getBlock(ignoreLiquids, ignorePassable, true, filter);
    }

    public Mono<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable,
                                boolean ignoreObstacles, Predicate<Block> filter) {
        int maxDistance = FastMath.min(100, (int) FastMath.ceil(this.distance));
        BlockIterator it = new BlockIterator(this.world, this.center, this.direction, this.size, maxDistance);
        while (it.hasNext()) {
            Block block = it.next();
            boolean passable = block.isPassable();
            if (passable) {
                if (block.isLiquid()) {
                    if (ignoreLiquids) {
                        continue;
                    }
                } else if (ignorePassable) {
                    continue;
                }
            }
            if (filter.test(block)) {
                return Mono.just(block);
            }
            if (!ignoreObstacles && !passable) {
                break;
            }
        }
        return Mono.empty();
    }

    public Mono<Entity> getEntity(Predicate<Entity> filter) {
        return this.getEntity(filter, this.distance);
    }

    @SneakyThrows
    public Mono<Entity> getEntity(Predicate<Entity> filter, double distance) {
        Vector startPos = this.center.toBukkitVector();
        Vector dir = this.direction.clone().normalize().multiply(distance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(this.size);
        return AxisAlignedBoundingBoxCollider.WORLD_SERVICE_CACHED_REFERENCE.get().orElseGet(Mono::empty)
                .flatMap(service -> service.getRepository(Entity.class, this.world))
                .cast(EntityRepository.class)
                .flatMapMany(worldRepository -> {
                    Location location = startPos.toLocation(this.world);
                    double radius = ImmutableVector.of(aabb.getMax()).maxComponent();
                    return worldRepository.getNearbyEntities(location, radius);
                })
                .collectList()
                .mapNotNull(entities -> {
                    Entity nearestHitEntity = null;
                    RayTraceResult nearestHitResult = null;
                    double nearestDistanceSq = Double.MAX_VALUE;

                    for (Entity entity : entities) {
                        if (!filter.test(entity)) continue;
                        BoundingBox boundingBox = entity.getBoundingBox().expand(this.size);
                        RayTraceResult hitResult = boundingBox.rayTrace(startPos, this.direction, this.distance);
                        if (hitResult != null) {
                            double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
                            if (distanceSq < nearestDistanceSq) {
                                nearestHitEntity = entity;
                                nearestHitResult = hitResult;
                                nearestDistanceSq = distanceSq;
                            }
                        }
                    }
                    if (nearestHitEntity == null) return null;
                    Vector hitPosition = nearestHitResult.getHitPosition();
                    BlockFace hitBlockFace = nearestHitResult.getHitBlockFace();
                    RayTraceResult rayTraceResult = new RayTraceResult(hitPosition, nearestHitEntity, hitBlockFace);
                    return rayTraceResult.getHitEntity();
                });
    }

    public Mono<ImmutableVector> getPosition(boolean ignoreEntity, boolean ignoreBlock,
                                             boolean ignoreLiquid, boolean ignorePassable,
                                             Predicate<Entity> entityFilter, Predicate<Block> blockFilter) {
        Mono<ImmutableVector> destination = Mono.empty();
        ImmutableVector position = this.center.add(this.direction.normalize().multiply(this.distance));
        if (!ignoreBlock) {
            destination = this.getBlock(ignoreLiquid, ignorePassable, true, blockFilter)
                    .mapNotNull(block -> this.center.add(this.direction.normalize()
                            .multiply(this.center
                                    .distance(ImmutableVector.of(block.getLocation().toCenterLocation())) - 0.5)));
        }
        if (!ignoreEntity) {
            destination = destination
                    .switchIfEmpty(Mono.just(this.center))
                    .map(this.center::distance)
                    .flatMap(distance -> this.getEntity(entityFilter, distance))
                    .mapNotNull(entity -> ImmutableVector.of(entity.getLocation())
                            .add(new ImmutableVector(0, entity.getHeight() / 2, 0)));
        }
        return destination.switchIfEmpty(Mono.just(position));
    }
}