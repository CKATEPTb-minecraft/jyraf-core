package dev.ckateptb.minecraft.jyraf.collider;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.collider.geometry.*;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class Colliders {
    private final Map<Tuple2<Class<? extends Collider<?>>, Class<? extends Collider<?>>>, ColliderIntersectHandler<?, ?>> intersects =
            Collections.synchronizedMap(new HashMap<>());

    static {
        registerIntersect(SphereBoundingBoxCollider.class, SphereBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return first.getLocation().toVector()
                    .isInSphere(second.getLocation().toVector(), second.getRadius() + first.getRadius());
        });
        registerIntersect(SphereBoundingBoxCollider.class, AxisAlignedBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return first.intersectsAABB(second);
        });
        registerIntersect(SphereBoundingBoxCollider.class, OrientedBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return second.intersectsSphere(first);
        });
        registerIntersect(AxisAlignedBoundingBoxCollider.class, AxisAlignedBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return first.intersectsAABB(second) || second.intersectsAABB(first);
        });
        registerIntersect(AxisAlignedBoundingBoxCollider.class, OrientedBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return second.intersectsAABB(first);
        });
        registerIntersect(OrientedBoundingBoxCollider.class, OrientedBoundingBoxCollider.class, (first, second) -> {
            if (!first.getWorld().equals(second.getWorld())) return false;
            return first.intersectsOBB(second);
        });
        registerIntersect(RayTraceCollider.class, RayTraceCollider.class, ((first, second) -> first.getObb().intersects(second.getObb())));
        registerIntersect(RayTraceCollider.class, AxisAlignedBoundingBoxCollider.class, ((first, second) -> first.getObb().intersects(second)));
        registerIntersect(RayTraceCollider.class, SphereBoundingBoxCollider.class, ((first, second) -> first.getObb().intersects(second)));
        registerIntersect(RayTraceCollider.class, OrientedBoundingBoxCollider.class, ((first, second) -> first.getObb().intersects(second)));
    }

    public synchronized <F extends Collider<F>, S extends Collider<S>> void registerIntersect(@NotNull Class<F> first,
                                                                                              @NotNull Class<S> second,
                                                                                              ColliderIntersectHandler<F, S> handler) {
        intersects.put(Tuples.of(first, second), handler);
    }

    @SuppressWarnings("unchecked")
    public synchronized <F extends Collider<F>, S extends Collider<S>, RF extends Collider<RF>, RS extends Collider<RS>> ColliderIntersectHandler<RF, RS> findIntersect(Class<F> first, Class<S> second) {
        Tuple2<Class<F>, Class<S>> objects = Tuples.of(first, second);
        if (intersects.containsKey(objects)) {
            return (ColliderIntersectHandler<RF, RS>) intersects.get(objects);
        }
        Tuple2<Class<S>, Class<F>> reversed = Tuples.of(second, first);
        if (intersects.containsKey(reversed)) {
            ColliderIntersectHandler<RF, RS> handler = (ColliderIntersectHandler<RF, RS>) intersects.get(reversed);
            return (f, s) -> handler.intersect((RF) s, (RS) f);
        }
        if (first.equals(CombinedBoundingBoxCollider.class)) {
            return Collider::intersects;
        }
        if (second.equals(CombinedBoundingBoxCollider.class)) {
            return (f, s) -> s.intersects(f);
        }
        return (f, s) -> {
            Jyraf.getPlugin().getLogger().warning("No collider intersect handler found for " + first + " and " + second);
            return false;
        };
    }

    public AxisAlignedBoundingBoxCollider aabb(World world, BoundingBox boundingBox) {
        Vector center = boundingBox.getCenter();
        Vector half = boundingBox.getMax().subtract(center);
        return aabb(center.toLocation(world), half);
    }

    public AxisAlignedBoundingBoxCollider aabb(@NotNull Entity entity) {
        Objects.requireNonNull(entity);
        BoundingBox boundingBox = entity.getBoundingBox();
        return aabb(entity.getWorld(), boundingBox);
    }

    public AxisAlignedBoundingBoxCollider aabb(@NotNull Block block) {
        Objects.requireNonNull(block);
        BoundingBox boundingBox = block.getBoundingBox();
        World world = block.getWorld();
        if (block.isSolid() || block.getType().isAir() || boundingBox.getVolume() != 0) {
            return aabb(world, boundingBox);
        }
        return aabb(block.getLocation().toCenterLocation());
    }

    public AxisAlignedBoundingBoxCollider aabb(@NotNull Location center) {
        return aabb(center, ImmutableVector.ONE.multiply(0.5));
    }

    public AxisAlignedBoundingBoxCollider aabb(@NotNull Location center, @NotNull Vector half) {
        return new AxisAlignedBoundingBoxCollider(center, half);
    }

    public SphereBoundingBoxCollider sphere(@NotNull Location center, double radius) {
        return new SphereBoundingBoxCollider(center, radius);
    }

    public CombinedBoundingBoxCollider combined(@NotNull CombinedBoundingBoxCollider.CombinedIntersectsMode mode, @NotNull Collider<?>... colliders) {
        return new CombinedBoundingBoxCollider(mode, colliders);
    }

    public CombinedBoundingBoxCollider disk(@NotNull OrientedBoundingBoxCollider obb, @NotNull SphereBoundingBoxCollider sphereCollider) {
        return new CombinedBoundingBoxCollider(CombinedBoundingBoxCollider.CombinedIntersectsMode.ALL, sphereCollider, obb);
    }

    public OrientedBoundingBoxCollider obb(@NotNull Location center, @NotNull Vector half, @NotNull EulerAngle angle) {
        return new OrientedBoundingBoxCollider(center, ImmutableVector.of(half), angle);
    }

    public RayTraceCollider ray(@NotNull LivingEntity entity, double distance, double size) {
        Location eyeLocation = entity.getEyeLocation();
        return ray(eyeLocation, eyeLocation.getDirection(), size, size, distance);
    }

    public RayTraceCollider ray(@NotNull Location start, @NotNull Vector direction, double width, double height, double distance) {
        return new RayTraceCollider(start, direction, width, height, distance);
    }

    public RayTraceCollider ray(@NotNull Location start, @NotNull Vector direction, Vector size) {
        return new RayTraceCollider(start, direction, size);
    }

    public interface ColliderIntersectHandler<F extends Collider<F>, S extends Collider<S>> {
        boolean intersect(F first, S second);
    }
}
