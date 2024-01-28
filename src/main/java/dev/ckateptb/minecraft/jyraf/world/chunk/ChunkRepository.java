package dev.ckateptb.minecraft.jyraf.world.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.world.WorldRepository;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ChunkRepository {
    // TODO Убирать запись если entity пропадает
    private final AsyncCache<Integer, Entity> entities = Caffeine.newBuilder().buildAsync();
    @Getter
    private final WorldRepository world;
    @Getter
    private final Long chunkKey;
    private final CachedReference<Chunk> chunk;

    public ChunkRepository(WorldRepository world, Long chunkKey) {
        this.world = world;
        this.chunkKey = chunkKey;
        this.chunk = new CachedReference<>(() -> this.world.getWorld().getChunkAt(this.chunkKey));
    }

    public Flux<Entity> getEntities() {
        return Flux.fromIterable(this.entities.asMap().values())
                .flatMap(Mono::fromFuture);
    }

    public Mono<Entity> removeEntity(Entity entity) {
        return Optional.ofNullable(this.entities.asMap().remove(entity.getEntityId()))
                .map(Mono::fromFuture)
                .orElse(Mono.empty());
    }

    public Mono<Entity> addEntity(Entity entity) {
        return Mono.fromFuture(this.entities.get(entity.getEntityId(), (key) -> entity));
    }

    public Chunk getChunk() {
        return this.chunk.get().orElseThrow();
    }
}
