package dev.ckateptb.minecraft.jyraf.world.lookup.entity;

import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import org.bukkit.entity.Entity;

public interface EntityLookup {
    Flux<Entity> getEntities();

    Mono<Entity> add(Entity entity);

    Mono<Entity> remove(Entity entity);

    void tick();
}
