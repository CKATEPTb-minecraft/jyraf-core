package dev.ckateptb.minecraft.jyraf.component;

import ink.glowing.text.InkyMessage;

class InkyComponent {
    private static final InkyMessage serializer = InkyMessage.inkyMessage();

    public static net.kyori.adventure.text.Component deserialize(String string) {
        return serializer.deserialize(string);
    }
}
