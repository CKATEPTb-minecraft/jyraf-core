package dev.ckateptb.minecraft.jyraf.world.lookup.entity;

import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityLookup {
    Flux<Entity> getEntities();

    Mono<Entity> add(Entity entity);

    Mono<Entity> remove(Entity entity);

    void tick();
}
