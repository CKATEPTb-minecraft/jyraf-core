package dev.ckateptb.minecraft.jyraf.repository.world.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class AbstractChunkRepository<K, T> implements ChunkRepository<T> {
    @Getter
    protected final Long chunkKey;
    @Getter
    @Setter
    private boolean loaded = true;
    protected final AsyncCache<K, T> entries = Caffeine.newBuilder().buildAsync();

    protected abstract K getKey(T entry);

    protected abstract boolean isValid(T entry);

    @Override
    public Mono<T> add(T entry) {
        return Mono.just(this.isValid(entry))
                .filter(Boolean::booleanValue)
                .flatMap(ignored -> Mono.fromFuture(this.entries.get(this.getKey(entry), uuid -> entry)))
                .switchIfEmpty(Mono.just(entry));
    }

    @Override
    public Mono<T> remove(T entry) {
        return Mono.justOrEmpty(this.entries.asMap().remove(this.getKey(entry))).flatMap(Mono::fromFuture);
    }

    @Override
    public Flux<T> get() {
        return Flux.fromIterable(this.entries.asMap().values()).flatMap(Mono::fromFuture);
    }
}
