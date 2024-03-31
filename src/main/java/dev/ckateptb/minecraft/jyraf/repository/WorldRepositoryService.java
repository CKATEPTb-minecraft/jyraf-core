package dev.ckateptb.minecraft.jyraf.repository;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.repository.entity.asynchronous.AsynchronousEntityRepository;
import dev.ckateptb.minecraft.jyraf.repository.packet.block.PacketBlockRepository;
import dev.ckateptb.minecraft.jyraf.repository.packet.entity.PacketEntityRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.WorldRepository;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class WorldRepositoryService implements Listener {
    private final AsyncCache<Tuple2<? extends Class<?>, UUID>, WorldRepository<?>> repositories = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<Class<?>, Tuple2<Plugin, Function<World, ? extends WorldRepository<?>>>> registrations = Caffeine.newBuilder().buildAsync();

    public WorldRepositoryService(Jyraf plugin) {
        this.register(plugin, Entity.class, AsynchronousEntityRepository::new);
        this.register(plugin, PacketEntity.class, PacketEntityRepository::new);
        this.register(plugin, PacketBlock.class, PacketBlockRepository::new);
    }

    @Schedule(async = true, initialDelay = 0, fixedRate = 1)
    private void tick() {
        Flux.fromIterable(this.repositories.asMap().values())
                .flatMap(Mono::fromFuture)
                .filter(repository -> repository instanceof Repository.Tickable)
                .cast(Repository.Tickable.class)
                .filter(Repository.Tickable::shouldTick)
                .subscribe(Repository.Tickable::tick);
    }

    @EventHandler
    public void on(ChunkLoadEvent event) {
        this.setChunkLoaded(event.getWorld().getUID(), event.getChunk().getChunkKey(), true);
    }

    @EventHandler
    public void on(ChunkUnloadEvent event) {
        this.setChunkLoaded(event.getWorld().getUID(), event.getChunk().getChunkKey(), false);
    }

    private void setChunkLoaded(UUID world, long chunkKey, boolean loaded) {
        Flux.defer(() -> Flux.fromIterable(this.repositories.asMap().values()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::fromFuture)
                .filter(repository -> repository.getWorld().getUID().equals(world))
                .flatMap(WorldRepository::getChunks)
                .filter(chunkRepository -> chunkRepository.getChunkKey().equals(chunkKey))
                .subscribe(chunkRepository -> chunkRepository.setLoaded(loaded));
    }

    public <T> void register(Plugin plugin, Class<T> clazz, Function<World, WorldRepository<T>> generator) {
        this.registrations.put(clazz, CompletableFuture.completedFuture(Tuples.of(plugin, generator)));
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<WorldRepository<T>> getRepository(Class<T> type, World world) {
        return Mono.justOrEmpty(this.registrations.getIfPresent(type))
                .flatMap(Mono::fromFuture)
                .map(tuple -> this.repositories.get(Tuples.of(type, world.getUID()), key -> {
                    Plugin plugin = tuple.getT1();
                    Function<World, ? extends WorldRepository<?>> generator = tuple.getT2();
                    WorldRepository<?> repository = generator.apply(world);
                    if (repository instanceof Listener listener) {
                        Bukkit.getPluginManager().registerEvents(listener, plugin);
                    }
                    return repository;
                }))
                .flatMap(Mono::fromFuture)
                .map(worldRepository -> (WorldRepository<T>) worldRepository);
    }
}
