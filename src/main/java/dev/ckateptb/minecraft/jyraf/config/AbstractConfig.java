package dev.ckateptb.minecraft.jyraf.config;

import dev.ckateptb.minecraft.jyraf.config.serializer.ConfigurationSerializers;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public abstract class AbstractConfig<N extends ScopedConfigurationNode<N>> implements Config {
    private transient boolean initialized;
    private transient ConfigurationLoader<N> configurationLoader;
    private transient ConfigurationNode configurationNode;

    public void initialize() {
        if (this.initialized) return;
        this.initialized = true;
        File file = getFile();
        File parent = file.getParentFile();
        String path = file.getAbsolutePath();
        if (!((parent.exists() || parent.mkdirs()) && parent.isDirectory())) {
            throw new RuntimeException(String.format("Could not load configuration file! Path: %s", path));
        }
        this.configurationLoader = getBuilder()
                .file(file)
                .defaultOptions(ConfigurationOptions.defaults()
                        .shouldCopyDefaults(true)
                        .implicitInitialization(true)
                        .serializers(TypeSerializerCollection.builder()
                                .registerAll(TypeSerializerCollection.defaults())
                                .registerAll(ConfigurationSerializers.getSerializers())
                                .register((type) -> true, ObjectMapper.factory().asTypeSerializer())
                                .build()
                        )
                ).build();
    }

    @SneakyThrows
    @Override
    public void load() {
        this.configurationNode = this.configurationLoader.load();
        Class<? extends Config> clazz = getClass();
        Object copy = this.configurationNode.get(clazz);
        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            field.set(this, field.get(copy));
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> declaredFields = Arrays.stream(clazz.getDeclaredFields()).toList();
        List<Field> fields = new ArrayList<>(declaredFields);
        Class<?> parent = clazz.getSuperclass();
        if (parent == null) return fields;
        List<Field> parentFields = getAllFields(parent);
        fields.addAll(parentFields);
        return fields.stream().filter(field -> {
            int modifiers = field.getModifiers();
            return !Modifier.isProtected(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isTransient(modifiers);
        }).toList();
    }

    @Override
    public void save() throws ConfigurateException {
        Class<? extends Config> clazz = getClass();
        this.configurationNode.set(clazz, this);
        this.configurationLoader.save(this.configurationNode);
    }

    protected abstract AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<N>>, ? extends AbstractConfigurationLoader<N>> getBuilder();
}
