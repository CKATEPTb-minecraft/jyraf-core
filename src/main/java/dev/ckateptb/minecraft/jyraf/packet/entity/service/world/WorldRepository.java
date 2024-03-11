package dev.ckateptb.minecraft.jyraf.packet.entity.service.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.service.lookup.PacketEntityLookup;
import dev.ckateptb.minecraft.jyraf.packet.entity.service.world.chunk.ChunkRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldRepository implements PacketEntityLookup {
    @Getter
    private final World world;
    private final AsyncCache<Long, ChunkRepository> chunks = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<UUID, Long> entityChunkCache = Caffeine.newBuilder().buildAsync();

    public void tick() {
        Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .doOnNext(ChunkRepository::tick)
                .flatMap(PacketEntityLookup::getEntities)
                .flatMap(entity -> {
                    UUID uuid = entity.getUniqueId();
                    return this.getCachedChunkKey(uuid).zipWith(Mono.just(entity));
                })
                .filter(objects -> {
                    Long previousChunkKey = objects.getT1();
                    PacketEntity entity = objects.getT2();
                    Location location = entity.getLocation();
                    Long chunkKey = Chunk.getChunkKey(location);
                    return !previousChunkKey.equals(chunkKey);
                })
                .map(Tuple2::getT2)
                .flatMap(this::removeEntity)
                .flatMap(this::addEntity)
                .subscribe();
    }

    @Override
    public Mono<PacketEntity> addEntity(PacketEntity entity) {
        World world = entity.getWorld();
        if (!world.getUID().equals(this.world.getUID())) return Mono.just(entity);
        Location location = entity.getLocation();
        long chunkKey = Chunk.getChunkKey(location);
        return Mono.fromFuture(this.chunks.get(chunkKey, ChunkRepository::new))
                .flatMap(chunkRepository -> {
                    this.entityChunkCache.put(entity.getUniqueId(), CompletableFuture.completedFuture(chunkKey));
                    return chunkRepository.addEntity(entity);
                });
    }

    @Override
    public Mono<PacketEntity> removeEntity(PacketEntity entity) {
        UUID uuid = entity.getUniqueId();
        return this.getCachedChunkKey(uuid)
                .flatMap(chunkKey -> Mono.fromFuture(this.chunks.get(chunkKey, ChunkRepository::new)))
                .flatMap(chunkRepository -> Mono.fromFuture(this.entityChunkCache.asMap().remove(uuid))
                        .map(chunkKey -> chunkRepository))
                .flatMap(chunkRepository -> chunkRepository.removeEntity(entity));
    }

    @Override
    public Flux<PacketEntity> getEntities() {
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(PacketEntityLookup::getEntities);
    }

    private Mono<Long> getCachedChunkKey(UUID uuid) {
        return Optional.ofNullable(this.entityChunkCache.getIfPresent(uuid))
                .map(Mono::fromFuture)
                .orElseGet(Mono::empty);
    }
}
