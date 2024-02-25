package dev.ckateptb.minecraft.jyraf.async.tracker.entity.world.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.async.tracker.entity.lookup.EntityLookup;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ChunkRepository implements EntityLookup {
    private final Long chunkKey;
    private final AsyncCache<UUID, Entity> entities = Caffeine.newBuilder().buildAsync();

    @Override
    public Mono<Entity> addEntity(Entity entity) {
        UUID uuid = entity.getUniqueId();
        Location location = entity.getLocation();
        Long chunkKey = Chunk.getChunkKey(location);
        if (!this.chunkKey.equals(chunkKey)) return Mono.just(entity);
        return Mono.fromFuture(this.entities.get(uuid, key -> entity));
    }

    @Override
    public Mono<Entity> removeEntity(Entity entity) {
        UUID uuid = entity.getUniqueId();
        return Mono.fromFuture(this.entities.asMap().remove(uuid));
    }

    @Override
    public Flux<Entity> getEntities() {
        return Flux.fromIterable(this.entities.asMap().values())
                .flatMap(Mono::fromFuture);
    }
}
