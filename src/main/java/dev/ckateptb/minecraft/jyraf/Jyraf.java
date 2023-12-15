package dev.ckateptb.minecraft.jyraf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import dev.ckateptb.minecraft.jyraf.command.inject.CommandInjection;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.database.inject.RepositoryInjection;
import dev.ckateptb.minecraft.jyraf.example.config.ConfigExample;
import dev.ckateptb.minecraft.jyraf.listener.ListenerInjection;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import dev.ckateptb.minecraft.jyraf.schedule.inject.ScheduleInjection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();

    @Getter
    private static Jyraf plugin;

    public Jyraf() {
        Jyraf.plugin = this;
        IoC.addComponentRegisterCallback(new ListenerInjection());
        IoC.addComponentRegisterCallback(new ScheduleInjection());
        IoC.addComponentRegisterCallback(new CommandInjection());
        IoC.addComponentRegisterCallback(new RepositoryInjection());
        IoC.addContainerInitializedCallback((container, count) -> {
                    plugin.getLogger().info("The " + container.getName() + " container has been initialized. Total " + count + " components.");
                    IoC.getBean(ConfigExample.class).orElse(Mono.empty()).subscribe(config -> {
                        if (!config.getDebug()) Logger.setGlobalLogLevel(Level.ERROR);
                    });
                }
        );
        IoC.scan(this);
    }

    public static Scheduler syncScheduler(Plugin plugin) {
        return SCHEDULER_CACHE.get(plugin, SyncScheduler::new);
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTask(this, IoC::initialize);
    }

    public Scheduler syncScheduler() {
        return syncScheduler(this);
    }
}