package dev.ckateptb.minecraft.jyraf.collider.geometry;

import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class OrientedBoundingBoxCollider implements Collider<OrientedBoundingBoxCollider> {
    protected final Location location;
    protected final EulerAngle rotation;
    protected final ImmutableVector right;
    protected final ImmutableVector up;
    protected final ImmutableVector forward;
    protected final ImmutableVector halfExtents;

    public OrientedBoundingBoxCollider(Location center, Vector halfExtents, EulerAngle eulerAngle) {
        this.location = center;
        this.rotation = new EulerAngle(eulerAngle.getX(), eulerAngle.getY(), 0);
        this.right = ImmutableVector.PLUS_I.rotate(this.rotation);
        this.up = ImmutableVector.PLUS_J.rotate(this.rotation);
        this.forward = ImmutableVector.PLUS_K.rotate(this.rotation);
        this.halfExtents = ImmutableVector.of(halfExtents);
    }


    @Override
    public @NotNull OrientedBoundingBoxCollider at(@NotNull Location center) {
        return new OrientedBoundingBoxCollider(center, this.halfExtents, this.rotation);
    }

    @Override
    public @NotNull OrientedBoundingBoxCollider grow(Vector vector) {
        return new OrientedBoundingBoxCollider(this.location, this.halfExtents.add(ImmutableVector.of(vector).abs()), this.rotation);
    }

    @Override
    public @NotNull OrientedBoundingBoxCollider scale(double amount) {
        return new OrientedBoundingBoxCollider(this.location, this.halfExtents.multiply(amount), this.rotation);
    }

    @Override
    public boolean contains(@NotNull Vector vector) {
        ImmutableVector point = ImmutableVector.of(vector);
        return getClosestPosition(point).distanceSquared(point) <= 0.01;
    }

    @Override
    public @NotNull Location getLocation() {
        return this.location.clone();
    }

    private ImmutableVector getClosestPosition(Vector target) {
        ImmutableVector closest = ImmutableVector.of(this.location);
        ImmutableVector destination = ImmutableVector.of(target).subtract(closest);
        for (int i = 0; i < 3; i++) {
            ImmutableVector axis = switch (i) {
                case 0 -> right;
                case 1 -> up;
                case 2 -> forward;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
            double halfComponent = halfExtents.getComponent(i);
            double dist = Math.clamp(-halfComponent, halfComponent, destination.dot(axis));
            closest = closest.add(axis.multiply(dist));
        }
        return closest;
    }

    public boolean intersectsAABB(AxisAlignedBoundingBoxCollider aabb) {
        return this.intersects(new OrientedBoundingBoxCollider(this.location, this.halfExtents, EulerAngle.ZERO))
                && aabb.contains(this.getClosestPosition(ImmutableVector.of(aabb.getLocation())));
    }

    public boolean intersectsSphere(SphereBoundingBoxCollider sphere) {
        ImmutableVector sphereCenter = ImmutableVector.of(sphere.getLocation());
        ImmutableVector distance = sphereCenter.subtract(getClosestPosition(sphereCenter));
        double radius = sphere.getRadius();
        return distance.dot(distance) <= radius * radius;
    }

    public boolean intersectsOBB(OrientedBoundingBoxCollider obb) {
        ImmutableVector centerDifference = ImmutableVector.of(obb.location).subtract(ImmutableVector.of(this.location));
        for (int i = 0; i < 15; i++) {
            ImmutableVector current = this.getByIndex(i, obb);
            if (projectionOnAxis(centerDifference, current) >
                    projectionOnAxis(this.right.multiply(this.halfExtents.getX()), current) +
                            projectionOnAxis(this.up.multiply(this.halfExtents.getY()), current) +
                            projectionOnAxis(this.forward.multiply(this.halfExtents.getZ()), current) +
                            projectionOnAxis(obb.right.multiply(obb.halfExtents.getX()), current) +
                            projectionOnAxis(obb.up.multiply(obb.halfExtents.getY()), current) +
                            projectionOnAxis(obb.forward.multiply(obb.halfExtents.getZ()), current)) {
                return false;
            }
        }
        return true;
    }

    private double projectionOnAxis(Vector vector, Vector vector2) {
        return FastMath.abs(vector.dot(vector2));
    }

    private ImmutableVector getByIndex(int n, OrientedBoundingBoxCollider other) {
        return switch (n) {
            case 0 -> this.right;
            case 1 -> this.up;
            case 2 -> this.forward;
            case 3 -> other.right;
            case 4 -> other.up;
            case 5 -> other.forward;
            case 6 -> this.right.getCrossProduct(other.right);
            case 7 -> this.right.getCrossProduct(other.up);
            case 8 -> this.right.getCrossProduct(other.forward);
            case 9 -> this.up.getCrossProduct(other.right);
            case 10 -> this.up.getCrossProduct(other.up);
            case 11 -> this.up.getCrossProduct(other.forward);
            case 12 -> this.forward.getCrossProduct(other.right);
            case 13 -> this.forward.getCrossProduct(other.up);
            case 14 -> this.forward.getCrossProduct(other.forward);
            default -> throw new IllegalStateException("Unexpected value: " + n);
        };
    }

    @Getter
    private final LazyLoader<Collection<ImmutableVector>> draw = LazyLoader.of(() -> {
        Collection<ImmutableVector> drawPoints = new ArrayList<>();
        drawPoints.add(ImmutableVector.of(this.getLocation()));

        // Получаем вершины объекта OBB
        ImmutableVector[] corners = this.getCorners();

        // Добавляем вершины к границам
        for (int i = 0; i < corners.length; i++) {
            drawPoints.add(corners[i]);
            drawPoints.add(corners[(i + 1) % corners.length]); // Добавляем ребра
        }

        return drawPoints;
    });

    public ImmutableVector[] getCorners() {
        ImmutableVector[] corners = new ImmutableVector[8];
        ImmutableVector locationVector = ImmutableVector.of(location);

        // Вычисляем вершины OBB
        for (int i = 0; i < 8; i++) {
            ImmutableVector offset = new ImmutableVector(
                    (i & 1) == 0 ? halfExtents.getX() : -halfExtents.getX(),
                    (i & 2) == 0 ? halfExtents.getY() : -halfExtents.getY(),
                    (i & 4) == 0 ? halfExtents.getZ() : -halfExtents.getZ()
            );
            corners[i] = locationVector.add(
                    right.multiply(offset.getX())
                            .add(up.multiply(offset.getY()))
                            .add(forward.multiply(offset.getZ()))
            );
        }

        return corners;
    }
}
