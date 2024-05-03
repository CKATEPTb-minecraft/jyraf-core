package dev.ckateptb.minecraft.jyraf.placeholder;

import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPI {
    private static final @NotNull LazyLoader<Boolean> cache = LazyLoader.of(() ->
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
    );

    public static @NotNull String setPlaceholders(@NotNull String text) {
        return setPlaceholders(null, text);
    }

    public static @NotNull String setPlaceholders(@Nullable Player player, @NotNull String text) {
        boolean enable = cache.get();
        if (!enable) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
