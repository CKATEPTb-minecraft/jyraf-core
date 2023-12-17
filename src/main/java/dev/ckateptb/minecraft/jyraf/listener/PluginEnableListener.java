package dev.ckateptb.minecraft.jyraf.listener;

import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PluginEnableListener implements Listener {
    @Getter
    private static final Map<Plugin, Set<Runnable>> executeOnEnable = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PluginEnableEvent event) {
        Plugin eventPlugin = event.getPlugin();
        if (executeOnEnable.containsKey(eventPlugin)) {
            Set<Runnable> runnables = executeOnEnable.get(eventPlugin);
            runnables.removeIf(runnable -> {
                try {
                    runnable.run();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return true;
            });
            if (runnables.isEmpty()) executeOnEnable.remove(eventPlugin);
        }
    }
}
