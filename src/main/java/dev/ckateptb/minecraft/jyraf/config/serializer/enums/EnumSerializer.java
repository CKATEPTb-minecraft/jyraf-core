package dev.ckateptb.minecraft.jyraf.config.serializer.enums;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class EnumSerializer implements TypeSerializer<Enum<?>> {
    @Override
    @SuppressWarnings("unchecked")
    public Enum<?> deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Class<? extends Enum<?>> theEnum = (Class<? extends Enum<?>>) GenericTypeReflector.erase(type).asSubclass(Enum.class);
        String name = node.getString();
        if (name == null) return null;
        for (Enum<?> constant : theEnum.getEnumConstants()) {
            String lower = constant.name();
            if (lower.equalsIgnoreCase(name)) {
                return constant;
            }
        }
        return null;
    }

    @Override
    public void serialize(Type type, @Nullable Enum<?> obj, ConfigurationNode node) throws SerializationException {
        node.raw(obj == null ? null : obj.name());
    }
}
