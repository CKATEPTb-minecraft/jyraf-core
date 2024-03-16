package dev.ckateptb.minecraft.jyraf.component.serialier.inkymessage;

import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import ink.glowing.text.InkyMessage;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class InkyComponentSerializer implements ComponentSerializer {
    private final InkyMessage serializer;

    public InkyComponentSerializer() {
        this(InkyMessage.inkyMessage());
    }


    public net.kyori.adventure.text.Component deserialize(String string) {
        return this.serializer.deserialize(string);
    }

    public String serialize(Component component) {
        return this.serializer.serialize(component);
    }
}
