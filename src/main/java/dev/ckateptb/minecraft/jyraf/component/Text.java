package dev.ckateptb.minecraft.jyraf.component;

import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import dev.ckateptb.minecraft.jyraf.component.serialier.inkymessage.InkyComponentSerializer;
import dev.ckateptb.minecraft.jyraf.component.serialier.minedown.MineDownComponentSerializer;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Text {
    private static ComponentSerializer SERIALIZER = Runtime.version().version().get(0) < 17 ?
            new MineDownComponentSerializer() :
            new InkyComponentSerializer();

    public static @NotNull Component of(@NotNull String string, @NotNull String... replacements) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(replacements);
        Validate.isTrue(replacements.length % 2 == 0);
        Component component = Text.SERIALIZER.deserialize(string);
        for (int i = 0; i < replacements.length; i += 2) {
            String match = replacements[i];
            String replacement = replacements[i + 1];
            component = component.replaceText(builder -> {
                builder.matchLiteral(match);
                builder.replacement(replacement);
            });
        }
        return component;
    }

    public static @NotNull String of(@NotNull Component component) {
        Objects.requireNonNull(component);
        return Text.SERIALIZER.serialize(component);
    }

    public static void setGlobalComponentSerializer(@NotNull ComponentSerializer serializer) {
        Objects.requireNonNull(serializer);
        Text.SERIALIZER = serializer;
    }
}
