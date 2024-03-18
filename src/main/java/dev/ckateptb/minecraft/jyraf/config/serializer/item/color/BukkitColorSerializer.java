package dev.ckateptb.minecraft.jyraf.config.serializer.item.color;

import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class BukkitColorSerializer implements TypeSerializer<Color> {

    @Override
    public Color deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return Color.fromRGB(
                node.node("red").getInt(0),
                node.node("green").getInt(0),
                node.node("blue").getInt(0)
        );
    }

    @Override
    public void serialize(Type type, @Nullable Color color, ConfigurationNode node) throws SerializationException {
        if (color == null) color = Color.BLACK;

        node.node("red").set(color.getRed());
        node.node("green").set(color.getGreen());
        node.node("blue").set(color.getBlue());
    }

}