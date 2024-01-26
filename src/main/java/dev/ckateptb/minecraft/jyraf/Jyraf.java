package dev.ckateptb.minecraft.jyraf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.closable.inject.ClosableInjection;
import dev.ckateptb.minecraft.jyraf.command.inject.CommandInjection;
import dev.ckateptb.minecraft.jyraf.config.inject.ConfigurationInjection;
import dev.ckateptb.minecraft.jyraf.config.serializer.ConfigurationSerializers;
import dev.ckateptb.minecraft.jyraf.config.serializer.UUIDSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.duration.DurationSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.enums.EnumSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.item.ItemStackSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.location.LocationSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.objectid.ObjectIdSerializer;
import dev.ckateptb.minecraft.jyraf.config.serializer.world.WorldSerializer;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.database.inject.RepositoryInjection;
import dev.ckateptb.minecraft.jyraf.database.types.inject.PersisterInjection;
import dev.ckateptb.minecraft.jyraf.listener.ListenerInjection;
import dev.ckateptb.minecraft.jyraf.listener.PluginStatusChangeListener;
import dev.ckateptb.minecraft.jyraf.packet.inject.PacketListenerInject;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import dev.ckateptb.minecraft.jyraf.schedule.inject.ScheduleInjection;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.threeten.extra.PeriodDuration;
import reactor.core.scheduler.Scheduler;

import java.util.UUID;

public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();

    @Getter
    private static Jyraf plugin;
    private CachedReference<PacketEventsAPI<Plugin>> packetAPI = new CachedReference<>(() -> {
        PacketEventsAPI<Plugin> packetAPI = SpigotPacketEventsBuilder.build(this);
        packetAPI.getSettings()
                .bStats(false)
                .checkForUpdates(false);
        return packetAPI;
    });

    public Jyraf() {
        Jyraf.plugin = this;
        //noinspection unchecked
        ConfigurationSerializers.registerSerializer((Class<Enum<?>>) (Object) Enum.class, new EnumSerializer());
        ConfigurationSerializers.registerSerializer(ItemStack.class, new ItemStackSerializer());
        ConfigurationSerializers.registerSerializer(PeriodDuration.class, new DurationSerializer());
        ConfigurationSerializers.registerSerializer(World.class, new WorldSerializer());
        ConfigurationSerializers.registerSerializer(Location.class, new LocationSerializer());
        ConfigurationSerializers.registerSerializer(ObjectId.class, new ObjectIdSerializer());
        ConfigurationSerializers.registerSerializer(UUID.class, new UUIDSerializer());
        Logger.setGlobalLogLevel(Level.ERROR);
        Server server = Bukkit.getServer();
        IoC.registerBean(this, server);
        IoC.registerBean(this, server.getPluginManager());
        IoC.addComponentRegisterHandler(new ListenerInjection());
        IoC.addComponentRegisterHandler(new PacketListenerInject());
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
    public void onLoad() {
        this.packetAPI.get().ifPresent(PacketEvents::setAPI);
    }

    @Override
    public void onEnable() {
        this.packetAPI.get().ifPresent(PacketEventsAPI::init);
        Bukkit.getPluginManager().registerEvents(new PluginStatusChangeListener(), this);
        IoC.initialize();
    }

    @Override
    public void onDisable() {
        this.packetAPI.get().ifPresent(PacketEventsAPI::terminate);
    }

    public Scheduler syncScheduler() {
        return syncScheduler(this);
    }

    public PacketEventsAPI<Plugin> getPacketApi() {
        return this.packetAPI.getIfPresent();
    }
}