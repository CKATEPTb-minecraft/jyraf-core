package dev.ckateptb.minecraft.jyraf.packet.entity.service.lookup;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PacketEntityLookup {

    Mono<PacketEntity> addEntity(PacketEntity entity);

    Mono<PacketEntity> removeEntity(PacketEntity entity);

    Flux<PacketEntity> getEntities();

    void tick();
}
