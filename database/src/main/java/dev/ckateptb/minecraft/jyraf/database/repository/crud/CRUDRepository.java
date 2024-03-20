package dev.ckateptb.minecraft.jyraf.database.repository.crud;

import com.j256.ormlite.dao.Dao;
import dev.ckateptb.minecraft.jyraf.database.repository.Repository;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.scheduler.Schedulers;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.util.Map;

public interface CRUDRepository<Entity, Id> extends Repository<Entity, Id> {
    @Override
    @SneakyThrows
    default Mono<Entity> findById(Id id) {
        return Mono.defer(() -> {
                    try {
                        return Mono.just(this.dao().queryForId(id));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }

    @Override
    default Flux<Entity> findBy(Entity template) {
        return Flux.defer(() -> {
                    try {
                        return Flux.fromIterable(this.dao().queryForMatching(template));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }

    @Override
    default Flux<Entity> findBy(Map<String, Object> template) {
        return Flux.defer(() -> {
                    try {
                        return Flux.fromIterable(this.dao().queryForFieldValues(template));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }

    @Override
    default Flux<Entity> findAll() {
        return Flux.defer(() -> {
                    try {
                        return Flux.fromIterable(this.dao().queryForAll());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }

    @Override
    default Mono<Entity> save(Entity entity) {
        return Mono.defer(() -> {
                    try {
                        return Mono.justOrEmpty(this.dao().createOrUpdate(entity));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic())
                .map(createOrUpdateStatus -> entity);
    }

    @Override
    default Mono<Boolean> delete(Id id) {
        return Mono.defer(() -> {
                    try {
                        return Mono.just(this.dao().deleteById(id));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic())
                .then(Mono.just(true));
    }

    @Override
    default Mono<Boolean> exists(Id id) {
        return Mono.defer(() -> {
                    try {
                        return Mono.just(this.dao().idExists(id));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }

    Dao<Entity, Id> dao();
}
