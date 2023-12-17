package dev.ckateptb.minecraft.jyraf.component;

import org.bukkit.ChatColor;

class LegacyComponent {
    public static net.kyori.adventure.text.Component deserialize(String string) {
        return net.kyori.adventure.text.Component.text(ChatColor.translateAlternateColorCodes('&', string));
    }
}
