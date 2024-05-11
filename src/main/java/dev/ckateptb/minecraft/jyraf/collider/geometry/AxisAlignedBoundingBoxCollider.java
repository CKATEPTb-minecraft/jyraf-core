package dev.ckateptb.minecraft.jyraf.collider.geometry;

import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AxisAlignedBoundingBoxCollider implements Collider<AxisAlignedBoundingBoxCollider> {
    protected final Location location;
    protected final ImmutableVector halfExtents;

    public AxisAlignedBoundingBoxCollider(@NotNull Location location, @NotNull Vector halfExtends) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(halfExtends);
        this.location = location.clone();
        this.halfExtents = ImmutableVector.of(halfExtends);
    }

    @Override
    public @NotNull AxisAlignedBoundingBoxCollider at(@NotNull Location location) {
        return new AxisAlignedBoundingBoxCollider(location, this.halfExtents);
    }

    @Override
    public @NotNull AxisAlignedBoundingBoxCollider grow(Vector size) {
        return new AxisAlignedBoundingBoxCollider(this.location, this.halfExtents.add(ImmutableVector.of(size).abs()));
    }

    @Override
    public @NotNull AxisAlignedBoundingBoxCollider scale(double multiplier) {
        return new AxisAlignedBoundingBoxCollider(this.location, this.halfExtents.multiply(multiplier));
    }

    @Override
    public @NotNull ImmutableVector getHalfExtents() {
        return this.halfExtents;
    }

    public ImmutableVector getMax() {
        return ImmutableVector.of(this.location).add(this.halfExtents);
    }

    public ImmutableVector getMin() {
        return ImmutableVector.of(this.location).subtract(this.halfExtents);
    }

    @Override
    public @NotNull Location getLocation() {
        return this.location.clone();
    }

    @Override
    public boolean contains(@NotNull Vector vector) {
        ImmutableVector center = ImmutableVector.of(this.location);
        ImmutableVector max = center.add(this.halfExtents);
        ImmutableVector min = center.subtract(this.halfExtents);
        return vector.isInAABB(min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AxisAlignedBoundingBoxCollider that)) return false;
        return Objects.equals(this.location, that.location) && Objects.equals(this.halfExtents, that.halfExtents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.location, this.halfExtents);
    }

    @Getter
    private final LazyLoader<Collection<ImmutableVector>> draw = LazyLoader.of(() -> {
        ImmutableVector[] corners = this.getCorners();
        List<ImmutableVector> bounds = new ArrayList<>();
        bounds.add(ImmutableVector.of(this.getLocation()));

        // Добавляем вершины к границам
        for (int i = 0; i < corners.length; i++) {
            bounds.add(corners[i]);
            bounds.add(corners[(i + 1) % corners.length]); // Добавляем ребра
        }

        return bounds;
    });

    private ImmutableVector[] getCorners() {
        ImmutableVector locationVector = ImmutableVector.of(this.location);
        ImmutableVector halfExtents = this.getHalfExtents();

        // Вычисляем вершины AABB
        ImmutableVector corner1 = locationVector.add(-halfExtents.getX(), -halfExtents.getY(), -halfExtents.getZ());
        ImmutableVector corner2 = locationVector.add(halfExtents.getX(), -halfExtents.getY(), -halfExtents.getZ());
        ImmutableVector corner3 = locationVector.add(-halfExtents.getX(), -halfExtents.getY(), halfExtents.getZ());
        ImmutableVector corner4 = locationVector.add(halfExtents.getX(), -halfExtents.getY(), halfExtents.getZ());
        ImmutableVector corner5 = locationVector.add(-halfExtents.getX(), halfExtents.getY(), -halfExtents.getZ());
        ImmutableVector corner6 = locationVector.add(halfExtents.getX(), halfExtents.getY(), -halfExtents.getZ());
        ImmutableVector corner7 = locationVector.add(-halfExtents.getX(), halfExtents.getY(), halfExtents.getZ());
        ImmutableVector corner8 = locationVector.add(halfExtents.getX(), halfExtents.getY(), halfExtents.getZ());

        // Возвращаем массив вершин
        return new ImmutableVector[]{corner1, corner2, corner3, corner4, corner5, corner6, corner7, corner8};
    }

    public boolean intersectsAABB(AxisAlignedBoundingBoxCollider other) {
        ImmutableVector center = ImmutableVector.of(this.getLocation());
        ImmutableVector otherCenter = ImmutableVector.of(other.getLocation());
        Vector min = center.subtract(this.halfExtents);
        Vector max = center.add(this.halfExtents);
        Vector otherMin = otherCenter.subtract(other.halfExtents);
        Vector otherMax = otherCenter.add(other.halfExtents);
        return min.getX() <= otherMax.getX()
                && max.getX() >= otherMin.getX()
                && min.getY() <= otherMax.getY()
                && max.getY() >= otherMin.getY()
                && min.getZ() <= otherMax.getZ()
                && max.getZ() >= otherMin.getZ();
    }
}