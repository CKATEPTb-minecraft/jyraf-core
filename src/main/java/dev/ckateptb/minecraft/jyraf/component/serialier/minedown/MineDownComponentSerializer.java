package dev.ckateptb.minecraft.jyraf.component.serialier.minedown;

import de.themoep.minedown.adventure.MineDown;
import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class MineDownComponentSerializer implements ComponentSerializer {
    @Override
    public Component deserialize(String string) {
        Component component = MineDown.parse(string);
        if (!string.contains("##")) {
            return component.decoration(TextDecoration.ITALIC, false);
        }
        return component;
    }

    @Override
    public String serialize(Component component) {
        return MineDown.stringify(component);
    }
}
