package dev.ckateptb.minecraft.jyraf.config.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDSerializer implements TypeSerializer<UUID> {
    @Override
    public UUID deserialize(Type type, ConfigurationNode node) {
        String string = node.getString();
        if (string == null) return null;
        return UUID.fromString(string);
    }

    @Override
    public void serialize(Type type, @Nullable UUID uuid, ConfigurationNode node) throws SerializationException {
        if (uuid == null) return;
        node.raw(uuid.toString());
    }

    @Override
    public @Nullable UUID emptyValue(Type specificType, ConfigurationOptions options) {
        return UUID.randomUUID();
    }
}
