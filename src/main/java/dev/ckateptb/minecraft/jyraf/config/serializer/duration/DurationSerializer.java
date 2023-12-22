package dev.ckateptb.minecraft.jyraf.config.serializer.duration;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.threeten.extra.PeriodDuration;

import java.lang.reflect.Type;

public class DurationSerializer implements TypeSerializer<PeriodDuration> {
    @Override
    public PeriodDuration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String string = node.getString();
        return string == null || string.isBlank() ? PeriodDuration.ZERO : PeriodDuration.parse(string);
    }

    @Override
    public void serialize(Type type, @Nullable PeriodDuration obj, ConfigurationNode node) throws SerializationException {
        if (obj != null) {
            node.raw(obj.toString());
        }
    }

    @Override
    public @Nullable PeriodDuration emptyValue(Type specificType, ConfigurationOptions options) {
        return PeriodDuration.ZERO;
    }
}
