package dev.ckateptb.minecraft.jyraf.math;

import com.google.common.primitives.Doubles;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.internal.commons.math3.geometry.euclidean.threed.Vector3D;
import dev.ckateptb.minecraft.jyraf.internal.commons.math3.util.FastMath;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ImmutableVector extends Vector {
    public static final ImmutableVector ZERO = new ImmutableVector(0, 0, 0);
    public static final ImmutableVector ONE = new ImmutableVector(1, 1, 1);
    public static final ImmutableVector PLUS_I = new ImmutableVector(1, 0, 0);
    public static final ImmutableVector MINUS_I = new ImmutableVector(-1, 0, 0);
    public static final ImmutableVector PLUS_J = new ImmutableVector(0, 1, 0);
    public static final ImmutableVector MINUS_J = new ImmutableVector(0, -1, 0);
    public static final ImmutableVector PLUS_K = new ImmutableVector(0, 0, 1);
    public static final ImmutableVector MINUS_K = new ImmutableVector(0, 0, -1);
    public static final ImmutableVector MIN_VELOCITY = new ImmutableVector(-4, -4, -4);
    public static final ImmutableVector MAX_VELOCITY = new ImmutableVector(4, 4, 4);

    public ImmutableVector() {
        super();
    }

    public ImmutableVector(double x, double y, double z) {
        super(x, y, z);
    }

    public ImmutableVector(int x, int y, int z) {
        super(x, y, z);
    }

    public ImmutableVector(float x, float y, float z) {
        super(x, y, z);
    }

    public static ImmutableVector of(Location location) {
        return new ImmutableVector(location.getX(), location.getY(), location.getZ());
    }

    public static ImmutableVector of(Vector vector) {
        return new ImmutableVector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static ImmutableVector of(Vector3D vector) {
        return new ImmutableVector(vector.getX(), vector.getY(), vector.getZ());
    }

    public ImmutableVector min(Vector other) {
        return new ImmutableVector(FastMath.min(x, other.getX()), FastMath.min(y, other.getY()), FastMath.min(z, other.getZ()));
    }

    public ImmutableVector max(Vector other) {
        return new ImmutableVector(FastMath.max(x, other.getX()), FastMath.max(y, other.getY()), FastMath.max(z, other.getZ()));
    }

    public ImmutableVector abs() {
        return new ImmutableVector(FastMath.abs(x), FastMath.abs(y), FastMath.abs(z));
    }

    public double minComponent() {
        return FastMath.min(FastMath.min(x, y), z);
    }

    public double maxComponent() {
        return FastMath.max(FastMath.max(x, y), z);
    }

    public double getComponent(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    public ImmutableVector negative() {
        return new ImmutableVector(-x, -y, -z);
    }

    public ImmutableVector radians() {
        return new ImmutableVector(FastMath.toRadians(x), FastMath.toRadians(y), FastMath.toRadians(z));
    }

    @Override
    public @NonNull ImmutableVector add(@NonNull Vector other) {
        return this.add(other.getX(), other.getY(), other.getZ());
    }

    public @NonNull ImmutableVector add(double x, double y, double z) {
        return new ImmutableVector(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public @NonNull ImmutableVector subtract(@NonNull Vector other) {
        return new ImmutableVector(x - other.getX(), y - other.getY(), z - other.getZ());
    }

    @Override
    public @NonNull ImmutableVector multiply(@NonNull Vector other) {
        return new ImmutableVector(x * other.getX(), y * other.getY(), z * other.getZ());
    }

    @Override
    public @NonNull ImmutableVector divide(@NonNull Vector other) {
        return new ImmutableVector(x / other.getX(), y / other.getY(), z / other.getZ());
    }

    @Override
    public @NonNull ImmutableVector copy(@NonNull Vector other) {
        return ImmutableVector.of(other);
    }

    @Override
    public float angle(@NonNull Vector other) {
        double dot = Doubles.constrainToRange(dot(other) / (length() * other.length()), -1.0, 1.0);

        return (float) FastMath.acos(dot);
    }

    @Override
    public @NonNull ImmutableVector midpoint(@NonNull Vector other) {
        return this.getMidpoint(other);
    }

    @Override
    public @NonNull ImmutableVector getMidpoint(@NonNull Vector other) {
        return new ImmutableVector((x + other.getX()) / 2, (y + other.getY()) / 2, (z + other.getZ()) / 2);
    }

    @Override
    public @NonNull ImmutableVector multiply(int m) {
        return new ImmutableVector(x * m, y * m, z * m);
    }

    @Override
    public @NonNull ImmutableVector multiply(double m) {
        return new ImmutableVector(x * m, y * m, z * m);
    }

    @Override
    public @NonNull ImmutableVector multiply(float m) {
        return new ImmutableVector(x * m, y * m, z * m);
    }

    @Override
    public @NonNull ImmutableVector crossProduct(@NonNull Vector other) {
        return this.getCrossProduct(other);
    }

    @Override
    public @NonNull ImmutableVector getCrossProduct(@NonNull Vector other) {
        return new ImmutableVector(y * other.getZ() - other.getY() * z, z * other.getX() - other.getZ() * x, x * other.getY() - other.getX() * y);
    }

    @Override
    public @NonNull ImmutableVector normalize() {
        double length = length();
        return new ImmutableVector(x / length, y / length, z / length);
    }

    public @NonNull ImmutableVector normalize(ImmutableVector defaultVector) {
        if (lengthSquared() == 0) return defaultVector;
        double length = length();
        return new ImmutableVector(x / length, y / length, z / length);
    }

    @Override
    public @NonNull ImmutableVector zero() {
        return ZERO;
    }

    @Override
    public boolean isNormalized() {
        return FastMath.abs(this.lengthSquared() - 1) < getEpsilon();
    }

    public @NonNull ImmutableVector rotate(EulerAngle eulerAngle) {
        return rotatePitch(eulerAngle.getX()).rotateYaw(eulerAngle.getY()).rotateRoll(eulerAngle.getZ());
    }

    public @NonNull ImmutableVector rotatePitch(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(x, angleCos * y - angleSin * z, angleSin * y + angleCos * z);
    }

    public @NonNull ImmutableVector rotateYaw(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(angleCos * x - angleSin * z, y, angleSin * x + angleCos * z);
    }

    public @NonNull ImmutableVector rotateRoll(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(angleCos * x + angleSin * y, angleSin * -x + angleCos * y, z);
    }

    @Override
    public @NonNull ImmutableVector rotateAroundX(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(x, angleCos * y - angleSin * z, angleSin * y + angleCos * z);
    }

    @Override
    public @NonNull ImmutableVector rotateAroundY(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(angleCos * x + angleSin * z, y, -angleSin * x + angleCos * z);
    }

    @Override
    public @NonNull ImmutableVector rotateAroundZ(double angle) {
        double angleCos = FastMath.cos(angle);
        double angleSin = FastMath.sin(angle);
        return new ImmutableVector(angleCos * x - angleSin * y, angleSin * x + angleCos * y, z);
    }

    @Override
    public @NonNull ImmutableVector rotateAroundAxis(@NonNull Vector axis, double angle) throws IllegalArgumentException {
        return this.rotateAroundNonUnitAxis(axis.isNormalized() ? axis : axis.normalize(), angle);
    }

    @Override
    public @NonNull ImmutableVector rotateAroundNonUnitAxis(@NonNull Vector axis, double angle) throws IllegalArgumentException {
        double x2 = axis.getX(), y2 = axis.getY(), z2 = axis.getZ();
        double cosTheta = FastMath.cos(angle);
        double sinTheta = FastMath.sin(angle);
        double dotProduct = this.dot(axis);
        double xPrime = x2 * dotProduct * (1d - cosTheta)
                + x * cosTheta
                + (-z2 * y + y2 * z) * sinTheta;
        double yPrime = y2 * dotProduct * (1d - cosTheta)
                + y * cosTheta
                + (z2 * x - x2 * z) * sinTheta;
        double zPrime = z2 * dotProduct * (1d - cosTheta)
                + z * cosTheta
                + (-y2 * x + x2 * y) * sinTheta;
        return new ImmutableVector(xPrime, yPrime, zPrime);
    }

    @Override
    public int getBlockX() {
        return (int) FastMath.floor(x);
    }


    @Override
    public int getBlockY() {
        return (int) FastMath.floor(y);
    }


    @Override
    public int getBlockZ() {
        return (int) FastMath.floor(z);
    }

    @Override
    public @NonNull ImmutableVector setX(int x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setX(double x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setX(float x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setY(int y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setY(double y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setY(float y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setZ(int z) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setZ(double z) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector setZ(float z) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NonNull ImmutableVector clone() {
        return ImmutableVector.of(super.clone());
    }

    public Vector3D toApacheVector() {
        return new Vector3D(x, y, z);
    }

    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }

    public EulerAngle toEulerAngle() {
        return new EulerAngle(x, y, z);
    }

    public EulerAngle directionToEulerAngle() {
        double yaw = FastMath.atan2(z, x);
        double pitch = FastMath.atan2(FastMath.sqrt(FastMath.pow(z, 2) + FastMath.pow(x, 2)), y) + Math.PI;
        return new EulerAngle(-pitch + FastMath.toRadians(90), yaw + FastMath.toRadians(90), 0);
    }

    public double getDistanceAboveGround(World world, boolean ignoreLiquids) {
        return Colliders.ray(world, this, ImmutableVector.MINUS_J, FastMath.min(world.getMaxHeight(), this.y), 0)
                .getFirstBlockOptional(ignoreLiquids, true)
                .map(entry -> y - Colliders.aabb(entry.getKey()).getMax().getY())
                .orElse(Double.MAX_VALUE);
    }
}