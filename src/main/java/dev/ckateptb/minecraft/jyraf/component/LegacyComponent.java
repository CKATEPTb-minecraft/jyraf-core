package dev.ckateptb.minecraft.jyraf.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

class LegacyComponent {
    public static net.kyori.adventure.text.Component deserialize(String string) {
        return net.kyori.adventure.text.Component.text(ChatColor.translateAlternateColorCodes('&', string));
    }

    public static String serialize(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
}
