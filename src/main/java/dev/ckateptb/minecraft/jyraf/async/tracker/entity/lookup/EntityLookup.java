package dev.ckateptb.minecraft.jyraf.async.tracker.entity.lookup;

import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityLookup {

    Mono<Entity> addEntity(Entity entity);

    Mono<Entity> removeEntity(Entity entity);

    Flux<Entity> getEntities();
}
