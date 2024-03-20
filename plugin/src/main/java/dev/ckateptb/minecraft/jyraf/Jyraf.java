package dev.ckateptb.minecraft.jyraf;

import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.internal.cache.Cache;
import dev.ckateptb.minecraft.jyraf.internal.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.listener.PluginStatusChangeListener;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

// TODO:
//  Redis database
//  Temporary system (blocks, effects, callbacks, etc.)
//  Packet block
//  Packet boss bar
//  Packet scoreboard
//  Split by subprojects for better dependency control
public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();

    private final static CachedReference<GsonConfigurationLoader> GSON_MAPPER = new CachedReference<>(() ->
            GsonConfigurationLoader.builder().defaultOptions(ConfigurationOptions.defaults()
                    .shouldCopyDefaults(true)
                    .implicitInitialization(true)
                    .serializers(TypeSerializerCollection.builder()
                            .registerAll(TypeSerializerCollection.defaults())
                            .registerAll(ConfigurationSerializers.getSerializers())
                            .register((type) -> true, ObjectMapper.factory().asTypeSerializer())
                            .build()
                    )
            ).build());

    @Getter
    private static Jyraf plugin;
    private final CachedReference<PacketEventsAPI<Plugin>> packetAPI = new CachedReference<>(() -> {
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
        ConfigurationSerializers.registerSerializer(Color.class, new BukkitColorSerializer());
        ConfigurationSerializers.registerSerializer(PotionEffect.class, new PotionEffectSerializer());
        ConfigurationSerializers.registerSerializer(PotionData.class, new PotionDataSerializer());
        ConfigurationSerializers.registerSerializer(ItemStack.class, new ItemStackSerializer());
        ConfigurationSerializers.registerSerializer(PeriodDuration.class, new DurationSerializer());
        ConfigurationSerializers.registerSerializer(World.class, new WorldSerializer());
        ConfigurationSerializers.registerSerializer(Location.class, new LocationSerializer());
        ConfigurationSerializers.registerSerializer(ObjectId.class, new ObjectIdSerializer());
        ConfigurationSerializers.registerSerializer(UUID.class, new UUIDSerializer());
        ConfigurationSerializers.registerSerializer(AttributeModifier.class, new AttributeModifierSerializer());
        Logger.setGlobalLogLevel(Level.ERROR);
        Server server = Bukkit.getServer();
        IoC.registerBean(this, server);
        IoC.registerBean(this, server.getPluginManager());
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
    public void onLoad() {
        this.packetAPI.get().ifPresent(PacketEvents::setAPI);
    }

    @Override
    public void onEnable() {
        PatheticMapper.initialize(this);
        this.packetAPI.get().ifPresent(PacketEventsAPI::init);
        Bukkit.getPluginManager().registerEvents(new PluginStatusChangeListener(), this);
        IoC.initialize();
    }

    @Override
    public void onDisable() {
        PatheticMapper.shutdown();
        this.packetAPI.get().ifPresent(PacketEventsAPI::terminate);
    }

    public Scheduler syncScheduler() {
        return syncScheduler(this);
    }

    public PacketEventsAPI<Plugin> getPacketApi() {
        return this.packetAPI.getIfPresent();
    }

    public static GsonConfigurationLoader getGsonMapper() {
        return GSON_MAPPER.get().orElse(null);
    }
}