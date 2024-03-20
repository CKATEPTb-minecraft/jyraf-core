package dev.ckateptb.minecraft.jyraf.listener;

import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PluginStatusChangeListener implements Listener {
    @Getter
    private static final Map<Plugin, Set<Runnable>> executeOnDisable = new ConcurrentHashMap<>();
    @Getter
    private static final Map<Plugin, Set<Runnable>> executeOnEnable = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PluginEnableEvent event) {
        this.on(event, executeOnEnable);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PluginDisableEvent event) {
        this.on(event, executeOnDisable);
    }

    private void on(PluginEvent event, Map<Plugin, Set<Runnable>> map) {
        Plugin eventPlugin = event.getPlugin();
        if (map.containsKey(eventPlugin)) {
            Set<Runnable> runnables = map.get(eventPlugin);
            runnables.removeIf(runnable -> {
                try {
                    runnable.run();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return true;
            });
            if (runnables.isEmpty()) map.remove(eventPlugin);
        }
    }
}
