package dev.ckateptb.minecraft.jyraf.container.handler;

import org.bukkit.plugin.Plugin;

public interface ComponentRegisterHandler {
    void handle(Object component, String qualifier, Plugin owner);
}
