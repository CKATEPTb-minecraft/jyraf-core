package dev.ckateptb.minecraft.jyraf.inject;

import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.listener.PluginStatusChangeListener;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class ClosableInjection implements ComponentRegisterHandler {
    @Override
    public void handle(Object object, String qualifier, Plugin owner) {
        if (!(object instanceof AutoCloseable closeable)) return;
        PluginStatusChangeListener.getExecuteOnDisable().computeIfAbsent(owner, key -> ConcurrentHashMap.newKeySet()).add(() -> {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
