package dev.ckateptb.minecraft.jyraf.component;

import ink.glowing.text.InkyMessage;
import org.apache.commons.lang3.Validate;

public class Component {
    private static final InkyMessage serializer = InkyMessage.inkyMessage();

    public static net.kyori.adventure.text.Component of(String string, String... replacements) {
        Validate.isTrue(replacements.length % 2 == 0);
        net.kyori.adventure.text.Component deserialize = serializer.deserialize(string);
        for (int i = 0; i < replacements.length; i += 2) {
            String match = replacements[i];
            String replacement = replacements[i + 1];
            deserialize = deserialize.replaceText(builder -> {
                builder.matchLiteral(match);
                builder.replacement(replacement);
            });
        }
        return deserialize;
    }
}
