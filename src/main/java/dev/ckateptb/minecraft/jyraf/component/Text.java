package dev.ckateptb.minecraft.jyraf.component;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;

public class Text {
    private final static boolean legacy = Runtime.version().version().get(0) < 17;

    public static net.kyori.adventure.text.Component of(String string, String... replacements) {
        Validate.isTrue(replacements.length % 2 == 0);
        net.kyori.adventure.text.Component deserialize = legacy ?
                LegacyComponent.deserialize(string) :
                InkyComponent.deserialize(string);
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

    public static String of(Component component) {
        return legacy ?
                LegacyComponent.serialize(component) :
                InkyComponent.serialize(component);
    }
}
