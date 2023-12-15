package dev.ckateptb.minecraft.jyraf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.command.inject.CommandInjection;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.listener.ListenerInjection;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import dev.ckateptb.minecraft.jyraf.schedule.inject.ScheduleInjection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import reactor.core.scheduler.Scheduler;

public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();

    @Getter
    private static Jyraf plugin;

    public Jyraf() {
        Jyraf.plugin = this;
        IoC.addCallback(new ListenerInjection());
        IoC.addCallback(new ScheduleInjection());
        IoC.addCallback(new CommandInjection());
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