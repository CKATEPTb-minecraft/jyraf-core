package dev.ckateptb.minecraft.jyraf.config;

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

public abstract class AbstractConfig<N extends ScopedConfigurationNode<N>> implements Config {
    private final transient ConfigurationLoader<N> configurationLoader;
    private transient ConfigurationNode configurationNode;

    public AbstractConfig() {
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
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            field.set(this, field.get(copy));
        }
    }

    @Override
    public void save() throws ConfigurateException {
        Class<? extends Config> clazz = getClass();
        this.configurationNode.set(clazz, this);
        this.configurationLoader.save(this.configurationNode);
    }

    protected abstract AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<N>>, ? extends AbstractConfigurationLoader<N>> getBuilder();
}
