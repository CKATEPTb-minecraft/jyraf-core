package dev.ckateptb.minecraft.jyraf.config.serializer.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class WorldSerializer implements TypeSerializer<World> {
    @Override
    public World deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        if (string == null || string.isBlank()) {
            return null;
        }
        return Bukkit.getWorld(string);
    }

    @Override
    public void serialize(Type type, @Nullable World world, ConfigurationNode node) throws SerializationException {
        if (world != null) {
            node.raw(world.getName());
        }
    }
}
