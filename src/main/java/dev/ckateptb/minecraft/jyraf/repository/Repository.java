package dev.ckateptb.minecraft.jyraf.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Repository<T> {
    Mono<T> add(T entry);

    Mono<T> remove(T entry);

    Flux<T> get();

    public interface Tickable {
        void tick();

        boolean shouldTick();

        void shouldTick(boolean should);
    }
}
