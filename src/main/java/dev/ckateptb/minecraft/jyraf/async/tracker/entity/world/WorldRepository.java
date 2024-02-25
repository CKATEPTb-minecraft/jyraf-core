package dev.ckateptb.minecraft.jyraf.async.tracker.entity.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.async.tracker.entity.lookup.EntityLookup;
import dev.ckateptb.minecraft.jyraf.async.tracker.entity.world.chunk.ChunkRepository;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.colider.geometry.SphereBoundingBoxCollider;
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
import reactor.util.function.Tuple2;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldRepository implements EntityLookup {
    @Getter
    private final World world;
    private final AsyncCache<Long, ChunkRepository> chunks = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<UUID, Long> entityChunkCache = Caffeine.newBuilder().buildAsync();

    public void tick() {
        Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(EntityLookup::getEntities)
                .flatMap(entity -> {
                    UUID uuid = entity.getUniqueId();
                    return this.getCachedChunkKey(uuid).zipWith(Mono.just(entity));
                })
                .filter(objects -> {
                    Long previousChunkKey = objects.getT1();
                    Entity entity = objects.getT2();
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
    public Mono<Entity> addEntity(Entity entity) {
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
    public Mono<Entity> removeEntity(Entity entity) {
        UUID uuid = entity.getUniqueId();
        return this.getCachedChunkKey(uuid)
                .flatMap(chunkKey -> Mono.fromFuture(this.chunks.get(chunkKey, ChunkRepository::new)))
                .flatMap(chunkRepository -> Mono.fromFuture(this.entityChunkCache.asMap().remove(uuid))
                        .map(chunkKey -> chunkRepository))
                .flatMap(chunkRepository -> chunkRepository.removeEntity(entity));
    }

    @Override
    public Flux<Entity> getEntities() {
        return Flux.fromIterable(this.chunks.asMap().values())
                .flatMap(Mono::fromFuture)
                .flatMap(EntityLookup::getEntities);
    }

    private Mono<Long> getCachedChunkKey(UUID uuid) {
        return Optional.ofNullable(this.entityChunkCache.getIfPresent(uuid))
                .map(Mono::fromFuture)
                .orElseGet(Mono::empty);
    }

    public Flux<Entity> getNearbyEntities(Location location, double radius) {
        SphereBoundingBoxCollider sphere = Colliders.sphere(location, radius);
        return this.getNearbyChunks(location, radius, radius)
                .doFirst(() -> System.out.println("Starting entity search!"))
                .flatMap(ChunkRepository::getEntities)
                .filter(entity -> sphere.intersects(Colliders.aabb(entity)));
    }

    private Flux<ChunkRepository> getNearbyChunks(Location location, double xRadius, double zRadius) {
        System.out.println("Looking for chunks on " + location + " with radius " + xRadius);
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
        System.out.println("Chunks founded: " + set.size());
        return Flux.fromIterable(set)
                .flatMap(chunkKey -> Mono.fromFuture(this.chunks.get(chunkKey, ChunkRepository::new)));
    }
}
