package dev.ckateptb.minecraft.jyraf.collider.geometry;

import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Stream;

public class CombinedBoundingBoxCollider implements Collider<CombinedBoundingBoxCollider> {
    private final List<Tuple2<Collider<?>, ImmutableVector>> colliders;
    private final @NotNull CombinedIntersectsMode mode;

    public CombinedBoundingBoxCollider(@NotNull CombinedIntersectsMode mode, @NotNull Collider<?>... colliders) {
        this(mode, Arrays.asList(colliders));
    }

    public CombinedBoundingBoxCollider(@NotNull CombinedIntersectsMode mode, @NotNull Collection<Collider<?>> colliders) {
        Objects.requireNonNull(mode);
        Objects.requireNonNull(colliders);
        Validate.notEmpty(colliders);
        this.mode = mode;
        ImmutableVector center = this.calculateCenter(colliders);
        this.colliders = Collections.synchronizedList(colliders.stream()
                .map(collider -> {
                    ImmutableVector offset = ImmutableVector.of(collider.getLocation().subtract(center));
                    return Tuples.<Collider<?>, ImmutableVector>of(collider, offset);
                })
                .toList());
    }

    private ImmutableVector calculateCenter(Collection<Collider<?>> colliders) {
        double totalX = 0.0;
        double totalY = 0.0;
        double totalZ = 0.0;

        for (Collider<?> collider : colliders) {
            Location center = collider.getLocation();
            totalX += center.getX();
            totalY += center.getY();
            totalZ += center.getZ();
        }
        int size = colliders.size();
        return new ImmutableVector(totalX / size, totalY / size, totalZ / size);
    }

    @Override
    public synchronized @NotNull CombinedBoundingBoxCollider at(@NotNull Location location) {
        List<Collider<?>> newColliders = new ArrayList<>();
        for (Tuple2<Collider<?>, ImmutableVector> tuple2 : this.colliders) {
            Collider<?> collider = tuple2.getT1();
            ImmutableVector offset = tuple2.getT2();
            newColliders.add(collider.at(location.clone().add(offset)));
        }
        return new CombinedBoundingBoxCollider(this.mode, newColliders);
    }

    @Override
    public synchronized @NotNull CombinedBoundingBoxCollider grow(Vector size) {
        List<Collider<?>> newColliders = new ArrayList<>();
        for (Tuple2<Collider<?>, ImmutableVector> tuple2 : this.colliders) {
            Collider<?> collider = tuple2.getT1();
            newColliders.add(collider.grow(size));
        }
        return new CombinedBoundingBoxCollider(this.mode, newColliders);
    }

    @Override
    public synchronized @NotNull CombinedBoundingBoxCollider scale(double multiplier) {
        List<Collider<?>> newColliders = new ArrayList<>();
        for (Tuple2<Collider<?>, ImmutableVector> tuple2 : this.colliders) {
            Collider<?> collider = tuple2.getT1();
            Collider<?> scaledCollider = collider.scale(multiplier);
            if (scaledCollider != null) {
                newColliders.add(scaledCollider);
            }
        }
        return new CombinedBoundingBoxCollider(this.mode, newColliders);
    }

    @Override
    public synchronized @NotNull ImmutableVector getHalfExtents() {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (Tuple2<Collider<?>, ImmutableVector> tuple2 : this.colliders) {
            Collider<?> collider = tuple2.getT1();
            Vector halfExtents = collider.getHalfExtents();
            maxX = FastMath.max(maxX, halfExtents.getX());
            maxY = FastMath.max(maxY, halfExtents.getY());
            maxZ = FastMath.max(maxZ, halfExtents.getZ());
        }
        return new ImmutableVector(maxX, maxY, maxZ);
    }

    @Override
    public synchronized boolean intersects(@NotNull Collider<?> other) {
        return mode == CombinedIntersectsMode.ANY ? this.intersectsAny(other) : this.intersectsAll(other);
    }

    public synchronized boolean intersectsAny(Collider<?> other) {
        return this.getColliders().anyMatch(collider -> collider.intersects(other));
    }

    public synchronized boolean intersectsAll(Collider<?> other) {
        return this.getColliders().allMatch(collider -> collider.intersects(other));
    }

    @Override
    public synchronized boolean contains(@NotNull Vector vector) {
        return mode == CombinedIntersectsMode.ANY ? this.containsAny(vector) : this.containsAll(vector);
    }

    @Override
    public synchronized @NotNull Location getLocation() {
        Tuple2<Collider<?>, ImmutableVector> tuple2 = this.colliders.get(0);
        return tuple2.getT1().getLocation().subtract(tuple2.getT2());
    }

    @Getter
    private final LazyLoader<Collection<ImmutableVector>> draw = LazyLoader.of(() -> this.getColliders()
            .flatMap(collider -> collider.getDraw().get().stream().filter(this::contains))
            .toList());


    public synchronized boolean containsAny(Vector vector) {
        return this.getColliders().anyMatch(collider -> collider.contains(vector));
    }

    public synchronized boolean containsAll(Vector vector) {
        return this.getColliders().allMatch(collider -> collider.contains(vector));
    }

    public synchronized Stream<Collider<?>> getColliders() {
        return this.colliders.stream().map(Tuple2::getT1);
    }

    public enum CombinedIntersectsMode {
        ANY,
        ALL
    }
}
