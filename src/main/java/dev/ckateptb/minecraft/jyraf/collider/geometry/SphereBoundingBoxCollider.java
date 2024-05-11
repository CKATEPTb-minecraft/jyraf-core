package dev.ckateptb.minecraft.jyraf.collider.geometry;

import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
public class SphereBoundingBoxCollider implements Collider<SphereBoundingBoxCollider> {
    protected final Location location;
    protected final double radius;

    public SphereBoundingBoxCollider(@NotNull Location location, double radius) {
        java.util.Objects.requireNonNull(location);
        this.location = location.clone();
        this.radius = radius;
    }

    @Override
    public @NotNull SphereBoundingBoxCollider at(@NotNull Location location) {
        return new SphereBoundingBoxCollider(location, this.radius);
    }

    @Override
    public @NotNull SphereBoundingBoxCollider grow(Vector size) {
        return new SphereBoundingBoxCollider(this.location, this.radius + size.length());
    }

    @Override
    public @NotNull SphereBoundingBoxCollider scale(double amount) {
        return new SphereBoundingBoxCollider(this.location, this.radius * amount);
    }

    @Override
    public @NotNull ImmutableVector getHalfExtents() {
        return new ImmutableVector(this.radius, this.radius, this.radius);
    }

    @NotNull
    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public boolean contains(@NotNull Vector vector) {
        return vector.isInSphere(ImmutableVector.of(this.location), this.radius);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SphereBoundingBoxCollider that)) return false;
        return Double.compare(this.radius, that.radius) == 0 && Objects.equals(this.location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.location, radius);
    }

    @Getter
    private final LazyLoader<Collection<ImmutableVector>> draw = LazyLoader.of(() -> {
        List<ImmutableVector> bounds = new ArrayList<>();
        bounds.add(ImmutableVector.of(this.getLocation()));

        // Разбиваем сферу на горизонтальные и вертикальные сегменты
        int horizontalSegments = 16; // Количество горизонтальных сегментов
        int verticalSegments = 8; // Количество вертикальных сегментов

        // Вычисляем углы широты и долготы для каждого сегмента
        double horizontalSegmentAngle = 2 * FastMath.PI / horizontalSegments;
        double verticalSegmentAngle = FastMath.PI / verticalSegments;

        Location location = this.getLocation();
        double radius = this.getRadius();

        // Создаем точки, образующие сферу
        for (int i = 0; i < verticalSegments; i++) {
            double latitude1 = i * verticalSegmentAngle - FastMath.PI / 2; // Угол широты для текущего сегмента
            double latitude2 = (i + 1) * verticalSegmentAngle - FastMath.PI / 2; // Угол широты для следующего сегмента

            for (int j = 0; j < horizontalSegments; j++) {
                double longitude1 = j * horizontalSegmentAngle; // Угол долготы для текущего сегмента
                double longitude2 = (j + 1) * horizontalSegmentAngle; // Угол долготы для следующего сегмента

                // Вычисляем координаты точек на сфере
                double x1 = location.getX() + radius * FastMath.cos(latitude1) * FastMath.cos(longitude1);
                double y1 = location.getY() + radius * FastMath.cos(latitude1) * FastMath.sin(longitude1);
                double z1 = location.getZ() + radius * FastMath.sin(latitude1);

                double x2 = location.getX() + radius * FastMath.cos(latitude1) * FastMath.cos(longitude2);
                double y2 = location.getY() + radius * FastMath.cos(latitude1) * FastMath.sin(longitude2);
                double z2 = location.getZ() + radius * FastMath.sin(latitude1);

                double x3 = location.getX() + radius * FastMath.cos(latitude2) * FastMath.cos(longitude1);
                double y3 = location.getY() + radius * FastMath.cos(latitude2) * FastMath.sin(longitude1);
                double z3 = location.getZ() + radius * FastMath.sin(latitude2);

                double x4 = location.getX() + radius * FastMath.cos(latitude2) * FastMath.cos(longitude2);
                double y4 = location.getY() + radius * FastMath.cos(latitude2) * FastMath.sin(longitude2);
                double z4 = location.getZ() + radius * FastMath.sin(latitude2);

                // Добавляем четыре точки для текущего сегмента к границам
                bounds.add(new ImmutableVector(x1, y1, z1));
                bounds.add(new ImmutableVector(x2, y2, z2));
                bounds.add(new ImmutableVector(x3, y3, z3));
                bounds.add(new ImmutableVector(x4, y4, z4));
            }
        }

        return bounds;
    });

    public boolean intersectsAABB(AxisAlignedBoundingBoxCollider aabb) {
        ImmutableVector center = ImmutableVector.of(aabb.getLocation());
        ImmutableVector min = center.subtract(aabb.getHalfExtents());
        ImmutableVector max = center.add(aabb.getHalfExtents());
        double x = FastMath.max(min.getX(), FastMath.min(this.location.getX(), max.getX()));
        double y = FastMath.max(min.getY(), FastMath.min(this.location.getY(), max.getY()));
        double z = FastMath.max(min.getZ(), FastMath.min(this.location.getZ(), max.getZ()));
        return this.contains(new ImmutableVector(x, y, z));
    }
}
