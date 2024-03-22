package dev.ckateptb.minecraft.jyraf.repository.entity.synchronous;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.repository.entity.EntityRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.WorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// TODO drop to external plugin
@RequiredArgsConstructor
public class SynchronousEntityRepository implements EntityRepository, WorldRepository<Entity> {
    private final World world;

    @Override
    public Mono<Entity> add(Entity entry) {
        return Mono.just(entry);
    }

    @Override
    public Mono<Entity> remove(Entity entry) {
        return Mono.just(entry);
    }

    @Override
    public Flux<Entity> get() {
        return Jyraf.synchronizedFlux(this.world::getEntities);
    }

    @Override
    public Flux<Entity> getNearbyEntities(Location location, double radius) {
        return Jyraf.synchronizedFlux(() -> this.world.getNearbyEntities(location, radius, radius, radius));
    }

    @Override
    public Mono<ChunkRepository<Entity>> getChunk(Long chunkKey) {
        return Mono.empty();
    }

    @Override
    public Flux<ChunkRepository<Entity>> getChunks() {
        return Flux.empty();
    }

    @Override
    public World getWorld() {
        return this.world;
    }
}
