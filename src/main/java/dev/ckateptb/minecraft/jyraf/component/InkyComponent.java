package dev.ckateptb.minecraft.jyraf.component;

import ink.glowing.text.InkyMessage;
import net.kyori.adventure.text.Component;

class InkyComponent {
    private static final InkyMessage serializer = InkyMessage.inkyMessage();

    public static net.kyori.adventure.text.Component deserialize(String string) {
        return serializer.deserialize(string);
    }

    public static String serialize(Component component) {
        return serializer.serialize(component);
    }
}
