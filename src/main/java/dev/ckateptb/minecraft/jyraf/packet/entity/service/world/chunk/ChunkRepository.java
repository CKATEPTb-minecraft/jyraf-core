package dev.ckateptb.minecraft.jyraf.packet.entity.service.world.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.service.lookup.PacketEntityLookup;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ChunkRepository implements PacketEntityLookup {
    private final Long chunkKey;
    private final AsyncCache<UUID, PacketEntity> entities = Caffeine.newBuilder().buildAsync();

    @Override
    public Mono<PacketEntity> addEntity(PacketEntity entity) {
        UUID uuid = entity.getUniqueId();
        Location location = entity.getLocation();
        Long chunkKey = Chunk.getChunkKey(location);
        if (!this.chunkKey.equals(chunkKey)) return Mono.just(entity);
        return Mono.fromFuture(this.entities.get(uuid, key -> entity));
    }

    @Override
    public Mono<PacketEntity> removeEntity(PacketEntity entity) {
        UUID uuid = entity.getUniqueId();
        return Mono.fromFuture(this.entities.asMap().remove(uuid));
    }

    @Override
    public Flux<PacketEntity> getEntities() {
        return Flux.fromIterable(this.entities.asMap().values())
                .flatMap(Mono::fromFuture);
    }

    @Override
    public void tick() {
        this.getEntities().subscribe(PacketEntity::tick);
    }

}
