package dev.ckateptb.minecraft.jyraf.math;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MathUtils {
    public static double lerp(double from, double to, double step) {
        return (1.0 - step) * from + step * to;
    }

    public static @NotNull ImmutableVector lerp(@NotNull Vector from, @NotNull Vector to, double step) {
        return new ImmutableVector(lerp(from.getX(), to.getX(), step), lerp(from.getY(), to.getY(), step), lerp(from.getZ(), to.getZ(), step));
    }

    public static double clamp(double value, double min, double max) {
        return FastMath.min(max, FastMath.max(min, value));
    }
}
