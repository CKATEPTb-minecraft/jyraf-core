package dev.ckateptb.minecraft.jyraf.component.serialier;

import net.kyori.adventure.text.Component;

public interface ComponentSerializer {
    net.kyori.adventure.text.Component deserialize(String string);

    String serialize(Component component);
}
