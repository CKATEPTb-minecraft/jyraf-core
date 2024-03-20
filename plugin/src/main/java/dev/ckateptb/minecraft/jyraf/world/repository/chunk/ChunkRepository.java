package dev.ckateptb.minecraft.jyraf.world.repository.chunk;

import dev.ckateptb.minecraft.jyraf.internal.cache.AsyncCache;
import dev.ckateptb.minecraft.jyraf.internal.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Flux;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.EntityLookup;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.PacketEntityLookup;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

@RequiredArgsConstructor
public class ChunkRepository implements EntityLookup, PacketEntityLookup {
    private final Long chunkKey;

    // Entity and PacketEntity map by UUID
    private final AsyncCache<UUID, Object> entities = Caffeine.newBuilder().buildAsync();

    @SuppressWarnings("unchecked")
    private <T> Mono<T> add(T object, UUID uuid, Location location) {
        Long chunkKey = Chunk.getChunkKey(location);
        if (!this.chunkKey.equals(chunkKey)) return Mono.just(object);
        return (Mono<T>) Mono.fromFuture(this.entities.get(uuid, key -> object));
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> remove(T ignored, UUID uuid) {
        return (Mono<T>) Mono.fromFuture(this.entities.asMap().remove(uuid));
    }

    @Override
    public Mono<Entity> add(Entity entity) {
        return this.add(entity, entity.getUniqueId(), entity.getLocation());
    }

    @Override
    public Mono<Entity> remove(Entity entity) {
        return this.remove(entity, entity.getUniqueId());
    }

    @Override
    public Mono<PacketEntity> add(PacketEntity entity) {
        return this.add(entity, entity.getUniqueId(), entity.getLocation());
    }

    @Override
    public Mono<PacketEntity> remove(PacketEntity entity) {
        return this.remove(entity, entity.getUniqueId());
    }

    @Override
    public void tick() {
        this.getPacketEntities().subscribe(PacketEntity::tick);
    }

    private Flux<Object> get() {
        return Flux.fromIterable(this.entities.asMap().values())
                .flatMap(Mono::fromFuture);
    }

    @Override
    public Flux<Entity> getEntities() {
        return this.get().filter(object -> object instanceof Entity).cast(Entity.class);
    }

    @Override
    public Flux<PacketEntity> getPacketEntities() {
        return this.get().filter(object -> object instanceof PacketEntity).cast(PacketEntity.class);
    }
}
