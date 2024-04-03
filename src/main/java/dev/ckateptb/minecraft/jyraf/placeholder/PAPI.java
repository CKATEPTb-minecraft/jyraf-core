package dev.ckateptb.minecraft.jyraf.placeholder;

import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPI {
    private static final @NotNull CachedReference<Boolean> cache = new CachedReference<>(() ->
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
    );

    public static @NotNull String setPlaceholders(@NotNull String text) {
        return setPlaceholders(null, text);
    }

    public static @NotNull String setPlaceholders(@Nullable Player player, @NotNull String text) {
        return cache.get().filter(enabled -> enabled)
                .map(enabled -> PlaceholderAPI.setPlaceholders(player, text))
                .orElse(text);
    }
}
