package dev.ckateptb.minecraft.jyraf.component;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

class LegacyComponent {
    public static net.kyori.adventure.text.Component deserialize(String string) {
        return MineDown.parse(string).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String serialize(Component component) {
        return MineDown.stringify(component);
    }
}
