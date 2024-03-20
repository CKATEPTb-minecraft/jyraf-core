package dev.ckateptb.minecraft.jyraf.database.repository;

import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public interface Repository<Entity, Id> extends AutoCloseable {
    Mono<Entity> findById(Id id);

    Flux<Entity> findBy(Entity template);

    Flux<Entity> findBy(Map<String, Object> template);

    Flux<Entity> findAll();

    Mono<Entity> save(Entity entity);

    Mono<Boolean> delete(Id id);

    Mono<Boolean> exists(Id id);

    void connect(Plugin owner, Class<Entity> entityClass, Class<Id> idClass);
}
