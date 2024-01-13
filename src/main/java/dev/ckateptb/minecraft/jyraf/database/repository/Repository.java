package dev.ckateptb.minecraft.jyraf.database.repository;

import com.j256.ormlite.dao.Dao;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
