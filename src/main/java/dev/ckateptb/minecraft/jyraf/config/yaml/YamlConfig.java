package dev.ckateptb.minecraft.jyraf.config.yaml;

import dev.ckateptb.minecraft.jyraf.config.AbstractConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public abstract class YamlConfig extends AbstractConfig<CommentedConfigurationNode> {
    @Override
    protected AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<CommentedConfigurationNode>>, ? extends AbstractConfigurationLoader<CommentedConfigurationNode>> getBuilder() {
        return YamlConfigurationLoader.builder();
    }
}