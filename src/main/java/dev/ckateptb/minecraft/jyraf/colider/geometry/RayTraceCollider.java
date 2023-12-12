package dev.ckateptb.minecraft.jyraf.colider.geometry;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.colider.Collider;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
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
        return new RayTraceCollider(world, ImmutableVector.of(center), direction, distance, size);
    }

    @Override
    public RayTraceCollider grow(Vector vector) {
        return new RayTraceCollider(world, center, direction, distance + vector.getZ(), size + FastMath.max(vector.getX(), vector.getY()));
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
        ImmutableVector immutableVector = new ImmutableVector(size, size, distance);
        final double _2PI = 2 * Math.PI;
        final double x = direction.getX();
        final double z = direction.getZ();
        float pitch, yaw;
        if (x == 0 && z == 0) {
            pitch = direction.getY() > 0 ? -90 : 90;
            yaw = 0;
        } else {
            double theta = Math.atan2(-x, z);
            yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);
            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            pitch = (float) Math.toDegrees(Math.atan(-direction.getY() / xz));
        }
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        return Colliders.obb(world, center.add(direction.multiply(distance)), immutableVector, eulerAngle);
    }

    public Optional<Map.Entry<Block, BlockFace>> getFirstBlock(boolean ignoreLiquids, boolean ignorePassable) {
        RayTraceResult traceResult = world.rayTraceBlocks(center.toLocation(world), direction, distance, ignoreLiquids ? FluidCollisionMode.NEVER : FluidCollisionMode.ALWAYS, ignorePassable);
        if (traceResult == null) return Optional.empty();
        Block block = traceResult.getHitBlock();
        BlockFace blockFace = traceResult.getHitBlockFace();
        return block == null || blockFace == null ? Optional.empty() : Optional.of(Map.entry(block, blockFace));
    }

    public Optional<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable, Predicate<Block> filter) {
        return this.getBlock(ignoreLiquids, ignorePassable, true, filter);
    }

    public Optional<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable, boolean ignoreObstacles, Predicate<Block> filter) {
        BlockIterator it = new BlockIterator(world, center, direction, size, Math.min(100, (int) Math.ceil(distance)));
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
                return Optional.of(block);
            }
            if (!ignoreObstacles && !passable) {
                break;
            }
        }
        return Optional.empty();
    }

    public Optional<Entity> getEntity(Predicate<Entity> filter) {
        return this.getEntity(filter, this.distance);
    }

    @SneakyThrows
    public Optional<Entity> getEntity(Predicate<Entity> filter, double distance) {
        Vector startPos = center.toBukkitVector();
        Vector dir = direction.clone().normalize().multiply(distance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(size);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<Collection<Entity>> reference = new AtomicReference<>();
        Jyraf.getPlugin().syncScheduler().schedule(() -> {
            reference.set(startPos.toLocation(world).getNearbyEntities(aabb.getMaxX(), aabb.getMaxY(), aabb.getMaxZ()));
            countDownLatch.countDown();
        });
        countDownLatch.await(); // TODO We shouldn't block thread
        Collection<Entity> entities = reference.get();

        Entity nearestHitEntity = null;
        RayTraceResult nearestHitResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (!filter.test(entity)) continue;
            BoundingBox boundingBox = entity.getBoundingBox().expand(size);
            RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, distance);
            if (hitResult != null) {
                double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity;
                    nearestHitResult = hitResult;
                    nearestDistanceSq = distanceSq;
                }
            }
        }
        RayTraceResult traceResult = (nearestHitEntity == null) ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
        if (traceResult == null) return Optional.empty();
        return Optional.ofNullable(traceResult.getHitEntity());
    }

    public Optional<Vector> getPosition(boolean ignoreEntity, boolean ignoreBlock, boolean ignoreLiquid, boolean ignorePassable, Predicate<Entity> entityFilter, Predicate<Block> blockFilter) {
        double distance = this.distance;
        Vector blockPosition = null;
        Vector entityPosition = null;
        Vector position = center.add(direction.normalize().multiply(distance));

        if (!ignoreBlock) {
            Optional<Block> optional = getBlock(ignoreLiquid, ignorePassable, true, blockFilter);
            if (optional.isPresent()) {
                Block block = optional.get();
                ImmutableVector immutableVector = ImmutableVector.of(block.getLocation().toCenterLocation());
                blockPosition = center.add(direction.normalize().multiply(center.distance(immutableVector) - 0.5));
                distance = center.distance(blockPosition);
            }
        }

        if (!ignoreEntity) {
            Optional<Entity> optional = getEntity(entityFilter, distance);
            if (optional.isPresent()) {
                Entity entity = optional.get();
                entityPosition = ImmutableVector.of(entity.getLocation()).add(new ImmutableVector(0, entity.getHeight() / 2, 0));
            }
        }
        return Optional.of(ImmutableVector.of(entityPosition == null ? blockPosition == null ? position : blockPosition : entityPosition));
    }
}