package dev.ckateptb.minecraft.jyraf.component.serialier;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ComponentSerializer {
    @NotNull
    net.kyori.adventure.text.Component deserialize(@NotNull String string);

    @NotNull
    String serialize(Component component);
}