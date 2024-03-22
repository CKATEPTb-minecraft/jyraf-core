package dev.ckateptb.minecraft.jyraf.world;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import dev.ckateptb.minecraft.jyraf.world.config.WorldConfig;
import dev.ckateptb.minecraft.jyraf.world.repository.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
public class WorldService implements Listener {
    private final boolean asyncEntityLookup;
    private final AsyncCache<UUID, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();

    public WorldService(WorldConfig config) {
        this.asyncEntityLookup = config.getAsyncEntityLookup();
    }

    public Mono<WorldRepository> getWorld(UUID uuid) {
        World world = Bukkit.getWorld(uuid);
        if (world == null) return Mono.empty();
        return this.getWorld(world);
    }

    public Mono<WorldRepository> getWorld(World world) {
        return Mono.fromFuture(this.worlds.get(world.getUID(), key ->
                new WorldRepository(world, this.asyncEntityLookup)));
    }

    @Schedule(async = true, initialDelay = 0, fixedRate = 1)
    private void tick() {
        Flux.fromIterable(this.worlds.asMap().values())
                .flatMap(Mono::fromFuture)
                .subscribe(WorldRepository::tick);
    }

    @EventHandler
    public void on(EntityAddToWorldEvent event) {
        this.handleEntity(event.getEntity(), true);
    }

    @EventHandler
    public void on(EntityRemoveFromWorldEvent event) {
        this.handleEntity(event.getEntity(), false);
    }

    private void handleEntity(Entity entity, boolean add) {
        if (!this.asyncEntityLookup) return;
        Mono.defer(() -> this.getWorld(entity.getWorld()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(worldRepository -> add ? worldRepository.add(entity) : worldRepository.remove(entity))
                .subscribe();
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
        Mono.defer(() -> Mono.justOrEmpty(this.worlds.getIfPresent(world)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::fromFuture)
                .map(WorldRepository::getChunks)
                .mapNotNull(cache -> cache.getIfPresent(chunkKey))
                .flatMap(Mono::fromFuture)
                .subscribe(chunkRepository -> chunkRepository.setLoaded(loaded));
    }
}
