package dev.ckateptb.minecraft.jyraf.world.repository.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.EntityLookup;
import dev.ckateptb.minecraft.jyraf.world.lookup.entity.PacketEntityLookup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ChunkRepository implements EntityLookup, PacketEntityLookup {
    private final World world;
    @Getter
    private final Long chunkKey;
    private final boolean asyncEntityLookup;

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
        CompletableFuture<Object> future = this.entities.asMap().remove(uuid);
        if (future == null) return Mono.empty();
        return (Mono<T>) Mono.fromFuture(future);
    }

    @Override
    public Mono<Entity> add(Entity entity) {
        if (!this.asyncEntityLookup) return Mono.just(entity);
        return this.add(entity, entity.getUniqueId(), entity.getLocation());
    }

    @Override
    public Mono<Entity> remove(Entity entity) {
        if (!this.asyncEntityLookup) return Mono.just(entity);
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
        if (!this.asyncEntityLookup) {
            return Flux.defer(() -> Flux.fromArray(this.world.getChunkAt(this.chunkKey).getEntities()))
                    .publishOn(Jyraf.getPlugin().syncScheduler());
        }
        return this.get().filter(object -> object instanceof Entity).cast(Entity.class);
    }

    public boolean isLoaded() {
        return this.world.isChunkLoaded(FastMath.toIntExact(this.chunkKey), (int) (this.chunkKey >> 32));
    }

    @Override
    public Flux<PacketEntity> getPacketEntities() {
        return this.get().filter(object -> object instanceof PacketEntity).cast(PacketEntity.class);
    }
}
