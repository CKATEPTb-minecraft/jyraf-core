package dev.ckateptb.minecraft.jyraf.listener;

import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class ListenerInjection implements ComponentRegisterCallback {
    private final PluginManager manager = Bukkit.getPluginManager();

    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        if (component instanceof Listener listener) {
            this.manager.registerEvents(listener, owner);
        }
    }
}
