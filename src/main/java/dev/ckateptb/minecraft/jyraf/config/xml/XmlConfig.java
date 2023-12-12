package dev.ckateptb.minecraft.jyraf.config.xml;

import dev.ckateptb.minecraft.jyraf.config.AbstractConfig;
import org.spongepowered.configurate.AttributedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;

public abstract class XmlConfig extends AbstractConfig<AttributedConfigurationNode> {
    @Override
    protected AbstractConfigurationLoader.Builder<? extends AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<AttributedConfigurationNode>>, ? extends AbstractConfigurationLoader<AttributedConfigurationNode>> getBuilder() {
        return XmlConfigurationLoader.builder();
    }
}