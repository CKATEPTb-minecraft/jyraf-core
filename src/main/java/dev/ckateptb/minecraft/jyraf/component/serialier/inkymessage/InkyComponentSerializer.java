package dev.ckateptb.minecraft.jyraf.component.serialier.inkymessage;

import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import ink.glowing.text.InkyMessage;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@RequiredArgsConstructor
public class InkyComponentSerializer implements ComponentSerializer {
    private final InkyMessage serializer;

    public InkyComponentSerializer() {
        this(InkyMessage.inkyMessage());
    }


    public net.kyori.adventure.text.@NotNull Component deserialize(@NotNull String string) {
        Objects.requireNonNull(string);
        return this.serializer.deserialize(string);
    }

    public @NotNull String serialize(@NotNull Component component) {
        Objects.requireNonNull(component);
        return this.serializer.serialize(component);
    }
}
