package dev.ckateptb.minecraft.jyraf.component.serialier.minedown;

import de.themoep.minedown.adventure.MineDown;
import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MineDownComponentSerializer implements ComponentSerializer {
    @Override
    public @NotNull Component deserialize(@NotNull String string) {
        Objects.requireNonNull(string);
        Component component = MineDown.parse(string);
        if (!string.contains("##")) {
            return component.decoration(TextDecoration.ITALIC, false);
        }
        return component;
    }

    @Override
    public @NotNull String serialize(@NotNull Component component) {
        Objects.requireNonNull(component);
        return MineDown.stringify(component);
    }
}
