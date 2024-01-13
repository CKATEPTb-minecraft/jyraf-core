package dev.ckateptb.minecraft.jyraf.placeholder;

import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPI {
    private static final CachedReference<Boolean> cache = new CachedReference<>(() ->
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
    );

    public static String setPlaceholders(String text) {
        return setPlaceholders(null, text);
    }

    public static String setPlaceholders(Player player, String text) {
        return cache.get().filter(enabled -> enabled)
                .map(enabled -> PlaceholderAPI.setPlaceholders(player, text))
                .orElse(text);
    }
}
