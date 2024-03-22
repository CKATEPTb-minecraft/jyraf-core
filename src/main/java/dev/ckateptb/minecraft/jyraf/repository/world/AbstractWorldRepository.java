package dev.ckateptb.minecraft.jyraf.repository.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AbstractWorldRepository<K, T> implements WorldRepository<T> {
    @Getter
    protected final World world;
    protected final AsyncCache<Long, ChunkRepository<T>> chunks = Caffeine.newBuilder().buildAsync();
    protected final AsyncCache<K, Long> cache = Caffeine.newBuilder().buildAsync();

    protected abstract boolean isValid(T entry);

    protected abstract K getKey(T entry);

    protected abstract long getChunkKey(T entry);

    protected Mono<Long> getCachedChunkKey(K key) {
        return Mono.justOrEmpty(this.cache.getIfPresent(key))
                .flatMap(Mono::fromFuture);
    }

    @Override
    public Mono<T> add(T entry) {
        if (!this.isValid(entry)) return Mono.just(entry);
        long chunkKey = this.getChunkKey(entry);
        return this.getChunk(chunkKey)
                .flatMap(chunkRepository -> {
                    this.cache.put(this.getKey(entry), CompletableFuture.completedFuture(chunkKey));
                    return chunkRepository.add(entry);
                });
    }

    @Override
    public Mono<T> remove(T entry) {
        long chunkKey = this.getChunkKey(entry);
        return this.getChunk(chunkKey)
                .flatMap(chunkRepository -> Mono.justOrEmpty(this.cache.asMap().remove(this.getKey(entry)))
                        .flatMap(Mono::fromFuture)
                        .flatMap(ignored -> chunkRepository.remove(entry)));
    }

    @Override
    public Flux<T> get() {
        return this.getChunks()
                .filter(ChunkRepository::isLoaded)
                .flatMap(Repository::get);
    }

    protected abstract ChunkRepository<T> createChunkRepository(Long chunkKey);

    @Override
    public Mono<ChunkRepository<T>> getChunk(Long chunkKey) {
        return Mono.fromFuture(this.chunks.get(chunkKey, this::createChunkRepository));
    }

    @Override
    public Flux<ChunkRepository<T>> getChunks() {
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture);
    }
}
