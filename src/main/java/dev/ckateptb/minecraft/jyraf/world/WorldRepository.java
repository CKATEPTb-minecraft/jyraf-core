package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.world.chunk.ChunkRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldRepository {
    private final AsyncCache<Long, ChunkRepository> chunks = Caffeine.newBuilder().buildAsync();
    @Getter
    private final World world;
    private final WorldService worldService;

    public Mono<Boolean> removeEntityFromWorld(Entity entity) {
        long chunkKey = Chunk.getChunkKey(entity.getLocation());
        return Optional.ofNullable(chunks.getIfPresent(chunkKey)).map(Mono::fromFuture)
                .orElse(Mono.empty()).flatMap(chunkRepository -> chunkRepository.removeAndCheckEmpty(entity)
                ).flatMap(empty -> {
                    if (empty) {
                         chunks.asMap().remove(chunkKey);
                    }
                    return Mono.just(isEmpty());
                });
    }

    public void addOrUpdate(Entity entity) {
        this.worldService.cacheChunkAndGetPreviousIfPresentOrElseCurrent(entity)
                .flatMap(previousChunkKey -> {
                    long chunkKey = Chunk.getChunkKey(entity.getLocation());
                    Mono<ChunkRepository> chunk = Mono.defer(() ->
                            Mono.fromFuture(this.chunks.get(chunkKey, key ->
                                    new ChunkRepository(this, key))));
                    if (previousChunkKey != chunkKey) {
                        CompletableFuture<ChunkRepository> previous = this.chunks.getIfPresent(previousChunkKey);
                        if (previous != null) {
                            System.out.println("change chunk");
                            return Mono.fromFuture(previous)
                                    .flatMap(chunkRepository -> chunkRepository.removeEntity(entity))
                                    .flatMap(value -> chunk);
                        }
                    }
                    return chunk;
                })
                .flatMap(chunkRepository -> chunkRepository.addEntity(entity))
                .subscribe();
    }

    public boolean isEmpty() {
        return chunks.asMap().isEmpty();
    }

    public Flux<Entity> getEntities() {
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(ChunkRepository::getEntities);
    }
}
