package dev.ckateptb.minecraft.jyraf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import dev.ckateptb.minecraft.jyraf.closable.inject.ClosableInjection;
import dev.ckateptb.minecraft.jyraf.command.inject.CommandInjection;
import dev.ckateptb.minecraft.jyraf.config.inject.ConfigurationInjection;
import dev.ckateptb.minecraft.jyraf.config.serializer.BukkitSerializers;
import dev.ckateptb.minecraft.jyraf.config.serializer.enums.EnumSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.item.ItemStackSerializer;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.database.inject.RepositoryInjection;
import dev.ckateptb.minecraft.jyraf.database.types.inject.PersisterInjection;
import dev.ckateptb.minecraft.jyraf.listener.ListenerInjection;
import dev.ckateptb.minecraft.jyraf.listener.PluginStatusChangeListener;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import dev.ckateptb.minecraft.jyraf.schedule.inject.ScheduleInjection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import reactor.core.scheduler.Scheduler;

public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();

    @Getter
    private static Jyraf plugin;

    public Jyraf() {
        Jyraf.plugin = this;
        //noinspection unchecked
        BukkitSerializers.registerSerializer((Class<Enum<?>>) (Object) Enum.class, new EnumSerializer());
        BukkitSerializers.registerSerializer(ItemStack.class, new ItemStackSerializer());
        Logger.setGlobalLogLevel(Level.ERROR);
        IoC.addComponentRegisterHandler(new ListenerInjection());
        IoC.addComponentRegisterHandler(new ScheduleInjection());
        IoC.addComponentRegisterHandler(new PersisterInjection());
        IoC.addComponentRegisterHandler(new ClosableInjection());
        IoC.addComponentRegisterHandler(new ConfigurationInjection());
        IoC.addComponentRegisterHandler(new RepositoryInjection());
        CommandInjection commandInjection = new CommandInjection();
        IoC.addComponentRegisterHandler(commandInjection);
        IoC.addContainerInitializedHandler(commandInjection);
        IoC.addContainerInitializedHandler((container, count) -> plugin.getLogger().info("The " + container.getName() + " container has been initialized. Total " + count + " components.")
        );
        IoC.scan(this, string -> !string.startsWith(Jyraf.class.getPackageName() + ".internal"));
    }

    public static Scheduler syncScheduler(Plugin plugin) {
        return SCHEDULER_CACHE.get(plugin, SyncScheduler::new);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PluginStatusChangeListener(), this);
        IoC.initialize();
    }

    public Scheduler syncScheduler() {
        return syncScheduler(this);
    }
}