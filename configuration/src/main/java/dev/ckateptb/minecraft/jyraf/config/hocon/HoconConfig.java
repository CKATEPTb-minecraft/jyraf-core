package dev.ckateptb.minecraft.jyraf.config.hocon;

import dev.ckateptb.minecraft.jyraf.config.AbstractConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

public abstract class HoconConfig extends AbstractConfig<CommentedConfigurationNode> {
    @Override
    protected AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<CommentedConfigurationNode>>, ? extends AbstractConfigurationLoader<CommentedConfigurationNode>> getBuilder() {
        return HoconConfigurationLoader.builder();
    }
}
