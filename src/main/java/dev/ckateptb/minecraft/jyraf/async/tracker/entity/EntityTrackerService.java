package dev.ckateptb.minecraft.jyraf.async.tracker.entity;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.async.tracker.entity.world.WorldRepository;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class EntityTrackerService implements Listener {
    private final AsyncCache<UUID, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();

    public Mono<WorldRepository> getWorld(UUID uuid) {
        return Optional.ofNullable(this.worlds.getIfPresent(uuid)).map(Mono::fromFuture).orElseGet(Mono::empty);
    }

    @Schedule(async = true, initialDelay = 0, fixedRate = 1)
    private void tick() {
        Flux.fromIterable(this.worlds.asMap().values())
                .flatMap(Mono::fromFuture)
                .subscribe(WorldRepository::tick);
    }

    @EventHandler
    public void on(EntityAddToWorldEvent event) {
        World world = event.getWorld();
        Mono.fromFuture(this.worlds.get(world.getUID(), (key) -> new WorldRepository(world)))
                .flatMap(worldRepository -> worldRepository.addEntity(event.getEntity()))
                .subscribe();
    }

    @EventHandler
    public void on(EntityRemoveFromWorldEvent event) {
        World world = event.getWorld();
        Mono.fromFuture(this.worlds.get(world.getUID(), (key) -> new WorldRepository(world)))
                .flatMap(worldRepository -> worldRepository.removeEntity(event.getEntity()))
                .subscribe();
    }

    @EventHandler
    public void on(WorldLoadEvent event) {
        World world = event.getWorld();
        UUID uid = world.getUID();
        WorldRepository worldRepository = new WorldRepository(world);
        this.worlds.put(uid, CompletableFuture.completedFuture(worldRepository));
    }

    @EventHandler
    public void on(WorldUnloadEvent event) {
        World world = event.getWorld();
        UUID uid = world.getUID();
        this.worlds.asMap().remove(uid);
    }
}
