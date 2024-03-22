package dev.ckateptb.minecraft.jyraf.world.repository;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.colider.geometry.SphereBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.EntityLookup;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.PacketEntityLookup;
import dev.ckateptb.minecraft.jyraf.world.repository.chunk.ChunkRepository;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldRepository implements EntityLookup, PacketEntityLookup {
    @Getter
    private final World world;
    private final boolean asyncEntityLookup;
    private final AsyncCache<Long, ChunkRepository> chunks = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<UUID, Long> entityChunkCache = Caffeine.newBuilder().buildAsync();

    public Mono<ChunkRepository> getChunk(Long chunkKey) {
        return Mono.fromFuture(this.chunks.get(chunkKey, key -> new ChunkRepository(this.world, key, this.asyncEntityLookup)));
    }

    @Override
    public void tick() {
        Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .filter(ChunkRepository::isLoaded)
                .doOnNext(ChunkRepository::tick)
                .flatMap(chunkRepository -> {
                    Flux<PacketEntity> packetEntities = chunkRepository.getPacketEntities();
                    if (!this.asyncEntityLookup) return packetEntities;
                    Flux<Entity> entities = chunkRepository.getEntities();
                    return Flux.merge(entities, packetEntities);
                })
                .filterWhen(object -> {
                    UUID uuid;
                    Location location;
                    if (object instanceof Entity entity) {
                        uuid = entity.getUniqueId();
                        location = entity.getLocation();
                    } else if (object instanceof PacketEntity entity) {
                        uuid = entity.getUniqueId();
                        location = entity.getLocation();
                    } else return Mono.just(false);
                    return this.getCachedChunkKey(uuid).map(chunkKey -> !chunkKey.equals(Chunk.getChunkKey(location)));
                })
                .flatMap(object -> {
                    if (object instanceof Entity entity) return this.remove(entity).flatMap(this::add);
                    else if (object instanceof PacketEntity entity) return this.remove(entity).flatMap(this::add);
                    else return Mono.empty();
                })
                .subscribe();
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> add(T object, UUID uuid, World world, Location location) {
        if (!world.getUID().equals(this.world.getUID())) return Mono.just(object);
        long chunkKey = Chunk.getChunkKey(location);
        return (Mono<T>) this.getChunk(chunkKey)
                .flatMap(chunkRepository -> {
                    this.entityChunkCache.put(uuid, CompletableFuture.completedFuture(chunkKey));
                    if (object instanceof Entity entity) return chunkRepository.add(entity);
                    else if (object instanceof PacketEntity entity) return chunkRepository.add(entity);
                    else return Mono.empty();
                });
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<T> remove(T object, UUID uuid) {
        return this.getCachedChunkKey(uuid)
                .flatMap(this::getChunk)
                .flatMap(chunkRepository -> (Mono<T>) Mono.fromFuture(this.entityChunkCache.asMap().remove(uuid))
                        .flatMap(chunkKey -> {
                            if (object instanceof Entity entity) return chunkRepository.remove(entity);
                            else if (object instanceof PacketEntity entity) return chunkRepository.remove(entity);
                            else return Mono.empty();
                        }));
    }

    @Override
    public Mono<PacketEntity> add(PacketEntity entity) {
        return this.add(entity, entity.getUniqueId(), entity.getWorld(), entity.getLocation());
    }

    @Override
    public Mono<PacketEntity> remove(PacketEntity entity) {
        return this.remove(entity, entity.getUniqueId());
    }

    @Override
    public Mono<Entity> add(Entity entity) {
        if (!this.asyncEntityLookup) return Mono.just(entity);
        return this.add(entity, entity.getUniqueId(), entity.getWorld(), entity.getLocation());
    }

    @Override
    public Mono<Entity> remove(Entity entity) {
        if (!this.asyncEntityLookup) return Mono.just(entity);
        return this.remove(entity, entity.getUniqueId());
    }

    @Override
    public Flux<Entity> getEntities() {
        if (!this.asyncEntityLookup) {
            return Flux.defer(() -> Flux.fromIterable(this.world.getEntities()))
                    .subscribeOn(Jyraf.getPlugin().syncScheduler());
        }
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(EntityLookup::getEntities);
    }

    @Override
    public Flux<PacketEntity> getPacketEntities() {
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(PacketEntityLookup::getPacketEntities);
    }


    private Mono<Long> getCachedChunkKey(UUID uuid) {
        return Optional.ofNullable(this.entityChunkCache.getIfPresent(uuid))
                .map(Mono::fromFuture)
                .orElseGet(Mono::empty);
    }

    public Flux<Entity> getNearbyEntities(Location location, double radius) {
        if (!this.asyncEntityLookup) {
            return Flux.defer(() -> Flux.fromIterable(this.world.getNearbyEntities(location, radius, radius, radius)))
                    .subscribeOn(Jyraf.getPlugin().syncScheduler());
        }
        SphereBoundingBoxCollider sphere = Colliders.sphere(location, radius);
        return this.getNearbyChunks(location, radius, radius)
                .flatMap(ChunkRepository::getEntities)
                .filter(entity -> sphere.intersects(Colliders.aabb(entity)));
    }

    private Flux<ChunkRepository> getNearbyChunks(Location location, double xRadius, double zRadius) {
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        LongSet set = new LongArraySet();
        for (double xx = x - xRadius; xx <= x + xRadius; xx += 8) {
            for (double zz = z - zRadius; zz <= z + zRadius; zz += 8) {
                set.add(Chunk.getChunkKey(new Location(world, xx, y, zz)));
            }
        }
        return Flux.fromIterable(set).flatMap(this::getChunk);
    }
}
