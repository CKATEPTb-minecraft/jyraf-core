package dev.ckateptb.minecraft.jyraf.packet.entity.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.entity.service.world.WorldRepository;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import org.bukkit.Bukkit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PacketEntityService {
    private final AsyncCache<UUID, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();

    public Mono<WorldRepository> getWorld(UUID uuid) {
        return Mono.fromFuture(this.worlds.get(uuid, key -> new WorldRepository(Bukkit.getWorld(uuid))));
    }

    @Schedule(async = true, initialDelay = 0, fixedRate = 1)
    private void tick() {
        Flux.fromIterable(this.worlds.asMap().values())
                .flatMap(Mono::fromFuture)
                .subscribe(WorldRepository::tick);
    }
}
