package dev.ckateptb.minecraft.jyraf.world.lookup.entity;

import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;

public interface PacketEntityLookup {
    Flux<PacketEntity> getPacketEntities();

    Mono<PacketEntity> add(PacketEntity entity);

    Mono<PacketEntity> remove(PacketEntity entity);

    void tick();
}
