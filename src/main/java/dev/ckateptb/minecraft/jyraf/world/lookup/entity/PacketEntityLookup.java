package dev.ckateptb.minecraft.jyraf.world.lookup.entity;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PacketEntityLookup {
    Flux<PacketEntity> getPacketEntities();

    Mono<PacketEntity> add(PacketEntity entity);

    Mono<PacketEntity> remove(PacketEntity entity);

    void tick();
}
