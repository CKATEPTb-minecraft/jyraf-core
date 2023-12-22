package dev.ckateptb.minecraft.jyraf.config.inject;

import dev.ckateptb.minecraft.jyraf.config.Config;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

public class ConfigurationInjection implements ComponentRegisterHandler {
    @Override
    @SneakyThrows
    public void handle(Object object, String qualifier, Plugin owner) {
        if (!(object instanceof Config config)) return;
        config.initialize();
        if (config instanceof Config.Autoloader) {
            config.load();
            config.save();
        }
    }

    @Override
    public boolean onEnable() {
        return false;
    }
}
