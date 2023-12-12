package dev.ckateptb.minecraft.jyraf.config.jackson;

import dev.ckateptb.minecraft.jyraf.config.AbstractConfig;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

public abstract class JacksonConfig extends AbstractConfig<BasicConfigurationNode> {
    @Override
    protected AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<BasicConfigurationNode>>, ? extends AbstractConfigurationLoader<BasicConfigurationNode>> getBuilder() {
        return JacksonConfigurationLoader.builder();
    }
}
