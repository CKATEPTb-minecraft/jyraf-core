package dev.ckateptb.minecraft.jyraf.world;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import dev.ckateptb.minecraft.jyraf.world.repository.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class WorldService implements Listener {
    private final AsyncCache<UUID, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();

    public Mono<WorldRepository> getWorld(UUID uuid) {
        World world = Bukkit.getWorld(uuid);
        if (world == null) return Mono.empty();
        return this.getWorld(world);
    }

    public Mono<WorldRepository> getWorld(World world) {
        return Mono.fromFuture(this.worlds.get(world.getUID(), key -> new WorldRepository(world)));
    }

    @Schedule(async = true, initialDelay = 0, fixedRate = 1)
    private void tick() {
        Flux.fromIterable(this.worlds.asMap().values())
                .flatMap(Mono::fromFuture)
                .subscribe(WorldRepository::tick);
    }

    @EventHandler
    public void on(EntityAddToWorldEvent event) {
        Entity entity = event.getEntity();
        this.getWorld(entity.getWorld())
                .flatMap(worldRepository -> worldRepository.add(entity))
                .subscribe();
    }

    @EventHandler
    public void on(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        this.getWorld(entity.getWorld())
                .flatMap(worldRepository -> worldRepository.remove(entity))
                .subscribe();
    }
}
