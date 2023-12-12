package dev.ckateptb.minecraft.jyraf.colider;

import dev.ckateptb.minecraft.jyraf.colider.geometry.*;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.function.Function;

public class Colliders {
    public static final Function<World, AxisAlignedBoundingBoxCollider> BLOCK = world -> Colliders.aabb(world, ImmutableVector.ZERO, ImmutableVector.ONE);

    public static AxisAlignedBoundingBoxCollider aabb(Entity entity) {
        ImmutableVector location = ImmutableVector.of(entity.getLocation());
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double halfWidth = 0.5 * entity.getWidth();
        ImmutableVector min = new ImmutableVector(x - halfWidth, y, z - halfWidth);
        ImmutableVector max = new ImmutableVector(x + halfWidth, y + entity.getHeight(), z + halfWidth);
        return new AxisAlignedBoundingBoxCollider(entity.getWorld(), min, max).at(location);
    }

    public static AxisAlignedBoundingBoxCollider aabb(Block block) {
        World world = block.getWorld();
        BoundingBox box = block.getBoundingBox();
        if (block.getType().isAir()) {
            return new AxisAlignedBoundingBoxCollider(world, ImmutableVector.ZERO, ImmutableVector.ZERO);
        }
        if (box.getVolume() == 0 || !block.isCollidable()) {
            return BLOCK.apply(world).at(block.getLocation().toCenterLocation());
        }
        ImmutableVector min = new ImmutableVector(box.getMinX(), box.getMinY(), box.getMinZ());
        ImmutableVector max = new ImmutableVector(box.getMaxX(), box.getMaxY(), box.getMaxZ());
        return new AxisAlignedBoundingBoxCollider(world, min, max);
    }

    public static AxisAlignedBoundingBoxCollider aabb(Location location) {
        return BLOCK.apply(location.getWorld()).at(location);
    }

    public static AxisAlignedBoundingBoxCollider aabb(World world, Vector half) {
        ImmutableVector max = ImmutableVector.of(half);
        return new AxisAlignedBoundingBoxCollider(world, max.negative(), max);
    }

    public static AxisAlignedBoundingBoxCollider aabb(World world, Vector min, Vector max) {
        return new AxisAlignedBoundingBoxCollider(world, ImmutableVector.of(min), ImmutableVector.of(max));
    }

    public static SphereBoundingBoxCollider sphere(Location center, double radius) {
        return sphere(center.getWorld(), ImmutableVector.of(center), radius);
    }

    public static SphereBoundingBoxCollider sphere(World world, Vector center, double radius) {
        return new SphereBoundingBoxCollider(world, center, radius);
    }

    public static CombinedBoundingBoxCollider combined(World world, CombinedBoundingBoxCollider.CombinedIntersectsMode mode, Collider... colliders) {
        return new CombinedBoundingBoxCollider(world, mode, colliders);
    }

    public static CombinedBoundingBoxCollider disk(World world, OrientedBoundingBoxCollider obb, SphereBoundingBoxCollider sphereCollider) {
        return new CombinedBoundingBoxCollider(world, CombinedBoundingBoxCollider.CombinedIntersectsMode.ALL, sphereCollider, obb);
    }

    public static OrientedBoundingBoxCollider obb(World world, Vector center, Vector max, EulerAngle eulerAngle) {
        return new OrientedBoundingBoxCollider(world, ImmutableVector.of(center), ImmutableVector.of(max), eulerAngle);
    }

    public static RayTraceCollider ray(LivingEntity entity, double distance, double size) {
        Location eyeLocation = entity.getEyeLocation();
        return ray(entity.getWorld(), eyeLocation.toVector(), eyeLocation.getDirection(), distance, size);
    }

    public static RayTraceCollider ray(World world, Vector center, Vector direction, double distance, double size) {
        return new RayTraceCollider(world, ImmutableVector.of(center), ImmutableVector.of(direction), distance, size);
    }
}
