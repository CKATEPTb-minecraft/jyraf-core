package dev.ckateptb.minecraft.jyraf.repository.entity.asynchronous;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.colider.geometry.SphereBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.entity.EntityRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.AbstractWorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;

import java.util.UUID;

public class AsynchronousEntityRepository extends AbstractWorldRepository<UUID, Entity> implements EntityRepository, Repository.Tickable {
    public AsynchronousEntityRepository(World world) {
        super(world);
    }

    @Override
    protected boolean isValid(Entity entry) {
        return this.world.getUID().equals(entry.getWorld().getUID());
    }

    @Override
    protected UUID getKey(Entity entry) {
        return entry.getUniqueId();
    }

    @Override
    protected long getChunkKey(Entity entry) {
        return Chunk.getChunkKey(entry.getLocation());
    }

    @Override
    protected ChunkRepository<Entity> createChunkRepository(Long chunkKey) {
        return new AsynchronousEntityChunkRepository(chunkKey);
    }

    @Override
    public Flux<Entity> getNearbyEntities(Location location, double radius) {
        SphereBoundingBoxCollider sphere = Colliders.sphere(location, radius);
        return this.getNearbyChunks(location, radius, radius)
                .flatMap(ChunkRepository::get)
                .filter(entity -> sphere.intersects(Colliders.aabb(entity)));
    }

    @Override
    public void tick() {
        this.getChunks()
                .flatMap(Repository::get)
                .filterWhen(entity -> this.getCachedChunkKey(entity.getUniqueId())
                        .map(chunkKey -> !chunkKey.equals(Chunk.getChunkKey(entity.getLocation()))))
                .flatMap(this::remove)
                .flatMap(this::add)
                .subscribe();
    }

    @Override
    public boolean shouldTick() {
        return true;
    }

    @Override
    public void shouldTick(boolean should) {

    }

    public static class AsynchronousEntityChunkRepository extends AbstractChunkRepository<UUID, Entity>
            implements EntityRepository {
        public AsynchronousEntityChunkRepository(Long chunkKey) {
            super(chunkKey);
        }

        @Override
        protected UUID getKey(Entity entry) {
            return entry.getUniqueId();
        }

        @Override
        protected boolean isValid(Entity entry) {
            Location location = entry.getLocation();
            long chunkKey = Chunk.getChunkKey(location);
            return this.chunkKey.equals(chunkKey);
        }

        @Override
        public Flux<Entity> getNearbyEntities(Location location, double radius) {
            SphereBoundingBoxCollider sphere = Colliders.sphere(location, radius);
            return this.get()
                    .filter(entity -> sphere.intersects(Colliders.aabb(entity)));
        }
    }
}
