package dev.ckateptb.minecraft.jyraf.environment;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Arrays;

public enum Environment {
    PAPER("com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration"),
    SPIGOT("org.spigotmc.SpigotConfig"),
    BUKKIT;

    private final static Cache<Environment, Boolean> CACHE = Caffeine.newBuilder().build();
    private final String[] checks;

    Environment(String... checks) {
        this.checks = checks;
    }

    public static boolean isPaper() {
        return Environment.PAPER.check();
    }

    public static boolean isSpigot() {
        return Environment.SPIGOT.check();
    }

    public static boolean isBukkit() {
        return Environment.BUKKIT.check();
    }

    public boolean check() {
        return CACHE.get(this, key -> this.checks.length == 0 || Arrays.stream(this.checks).anyMatch(clazz -> {
            try {
                Class.forName(clazz);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }));
    }
}
