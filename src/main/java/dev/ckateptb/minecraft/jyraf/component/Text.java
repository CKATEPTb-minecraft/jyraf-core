package dev.ckateptb.minecraft.jyraf.component;

import dev.ckateptb.minecraft.jyraf.component.serialier.ComponentSerializer;
import dev.ckateptb.minecraft.jyraf.component.serialier.inkymessage.InkyComponentSerializer;
import dev.ckateptb.minecraft.jyraf.component.serialier.minedown.MineDownComponentSerializer;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;

public class Text {
    private static ComponentSerializer SERIALIZER = Runtime.version().version().get(0) < 17 ?
            new MineDownComponentSerializer() :
            new InkyComponentSerializer();

    public static Component of(String string, String... replacements) {
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

    public static String of(Component component) {
        return Text.SERIALIZER.serialize(component);
    }

    public static void setGlobalComponentSerializer(ComponentSerializer serializer) {
        Text.SERIALIZER = serializer;
    }
}
