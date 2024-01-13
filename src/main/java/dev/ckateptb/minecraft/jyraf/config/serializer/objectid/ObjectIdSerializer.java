package dev.ckateptb.minecraft.jyraf.config.serializer.objectid;

import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ObjectIdSerializer implements TypeSerializer<ObjectId> {
    @Override
    public ObjectId deserialize(Type type, ConfigurationNode node) {
        String string = node.getString();
        if (string == null) return null;
        return new ObjectId(string);
    }

    @Override
    public void serialize(Type type, @Nullable ObjectId id, ConfigurationNode node) throws SerializationException {
        if (id == null) return;
        node.raw(id.toHexString());
    }
}
