package dev.ckateptb.minecraft.jyraf.config.serializer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

public class BukkitSerializers {
    private static final Cache<Class<?>, TypeSerializer<?>> CACHE = Caffeine.newBuilder().build();

    public static <T> boolean hasSerializer(Class<T> clazz) {
        return CACHE.asMap().containsKey(clazz);
    }

    public static <T> void registerSerializer(Class<T> clazz, TypeSerializer<T> serializer) {
        CACHE.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends TypeSerializer<T>> R getSerializer(Class<T> clazz) {
        return (R) CACHE.getIfPresent(clazz);
    }

    @SuppressWarnings("unchecked")
    public static TypeSerializerCollection getSerializers() {
        TypeSerializerCollection.Builder builder = TypeSerializerCollection.builder();
        CACHE.asMap().forEach((aClass, typeSerializer) -> {
            Class<Object> clazz = (Class<Object>) aClass;
            TypeSerializer<Object> serializer = (TypeSerializer<Object>) typeSerializer;
            builder.register(clazz, serializer);
        });
        return builder.build();
    }
}
