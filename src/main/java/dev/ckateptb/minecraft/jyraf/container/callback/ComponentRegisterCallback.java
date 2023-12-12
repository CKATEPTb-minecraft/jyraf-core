package dev.ckateptb.minecraft.jyraf.container.callback;

import org.bukkit.plugin.Plugin;

public interface ComponentRegisterCallback {
    void handle(Object component, String qualifier, Plugin owner);
}
