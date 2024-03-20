package dev.ckateptb.minecraft.jyraf.colider.geometry;

import com.google.common.base.Objects;
import dev.ckateptb.minecraft.jyraf.colider.Collider;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.internal.commons.math3.util.FastMath;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class SphereBoundingBoxCollider implements Collider {
    protected final World world;
    protected final ImmutableVector center;
    protected final double radius;

    public SphereBoundingBoxCollider(World world, Vector center, double radius) {
        this.world = world;
        this.center = ImmutableVector.of(center);
        this.radius = radius;
    }

    @Override
    public SphereBoundingBoxCollider at(Vector center) {
        return new SphereBoundingBoxCollider(world, center, radius);
    }

    @Override
    public Collider grow(Vector vector) {
        return new SphereBoundingBoxCollider(world, center, radius + FastMath.max(FastMath.max(vector.getX(), vector.getY()), vector.getZ()));
    }

    @Override
    public SphereBoundingBoxCollider scale(double amount) {
        return new SphereBoundingBoxCollider(world, center, radius * amount);
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return new ImmutableVector(radius, radius, radius);
    }

    @Override
    public boolean intersects(Collider other) {
        World otherWorld = other.getWorld();
        if (!otherWorld.equals(world)) return false;
        if (other instanceof SphereBoundingBoxCollider sphere) {
            return sphere.center.isInSphere(center, radius + sphere.radius);
        }
        if (other instanceof AxisAlignedBoundingBoxCollider aabb) {
            ImmutableVector min = aabb.min;
            ImmutableVector max = aabb.max;
            double x = FastMath.max(min.getX(), FastMath.min(center.getX(), max.getX()));
            double y = FastMath.max(min.getY(), FastMath.min(center.getY(), max.getY()));
            double z = FastMath.max(min.getZ(), FastMath.min(center.getZ(), max.getZ()));
            return contains(new ImmutableVector(x, y, z));
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
    public boolean contains(Vector vector) {
        return vector.isInSphere(center, radius);
    }

    @Override
    public SphereBoundingBoxCollider affectEntities(Consumer<Flux<Entity>> consumer) {
        this.wrapToAABB().affectEntities(flux -> consumer.accept(applyFilter(flux, Colliders::aabb)));
        return this;
    }

    @Override
    public SphereBoundingBoxCollider affectBlocks(Consumer<Flux<Block>> consumer) {
        this.wrapToAABB().affectBlocks(flux -> consumer.accept(applyFilter(flux, Colliders::aabb)));
        return this;
    }

    @Override
    public SphereBoundingBoxCollider affectLocations(Consumer<Flux<Location>> consumer) {
        this.wrapToAABB().affectLocations(flux -> consumer.accept(applyFilter(flux, Colliders::aabb)));
        return this;
    }

    private <T> Flux<T> applyFilter(Flux<T> flux, Function<T, Collider> getter) {
        return flux.filter(t -> {
            Collider aabb = getter.apply(t);
            return this.intersects(aabb);
        });
    }

    private Collider wrapToAABB() {
        ImmutableVector halfExtents = this.getHalfExtents();
        return Colliders.aabb(world, halfExtents.negative().add(center), halfExtents.add(center));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public ImmutableVector getCenter() {
        return center;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SphereBoundingBoxCollider that)) return false;
        return Double.compare(that.radius, radius) == 0 && Objects.equal(world, that.world) && Objects.equal(center, that.center);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(world, radius, center);
    }

    @Override
    public String toString() {
        return "SphereCollider{" + "world=" + world.getName() + ", radius=" + radius + ", center=" + center + '}';
    }
}
