package dev.ckateptb.minecraft.jyraf.repository.packet.entity;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.AbstractWorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PacketEntityRepository extends AbstractWorldRepository<UUID, PacketEntity> implements Repository.Tickable {
    public PacketEntityRepository(World world) {
        super(world);
    }

    @Override
    protected boolean isValid(PacketEntity entry) {
        return this.world.getUID().equals(entry.getWorld().getUID());
    }

    @Override
    protected UUID getKey(PacketEntity entry) {
        return entry.getUniqueId();
    }

    @Override
    protected long getChunkKey(PacketEntity entry) {
        return Chunk.getChunkKey(entry.getLocation());
    }

    @Override
    protected ChunkRepository<PacketEntity> createChunkRepository(Long chunkKey) {
        return new PacketEntityChunkRepository(chunkKey);
    }

    @Override
    public void tick() {
        this.getChunks()
            .cast(PacketEntityChunkRepository.class)
            .filter(PacketEntityChunkRepository::shouldTick)
            .doOnNext(PacketEntityChunkRepository::tick)
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

    public static class PacketEntityChunkRepository extends AbstractChunkRepository<UUID, PacketEntity> implements Tickable {

        public PacketEntityChunkRepository(Long chunkKey) {
            super(chunkKey);
        }

        @Override
        protected UUID getKey(PacketEntity entry) {
            return entry.getUniqueId();
        }

        @Override
        protected boolean isValid(PacketEntity entry) {
            Location location = entry.getLocation();
            long chunkKey = Chunk.getChunkKey(location);
            return this.chunkKey.equals(chunkKey);
        }

        @Override
        public void tick() {
            this.get().subscribe(PacketEntity::tick);
        }

        @Override
        public Mono<PacketEntity> remove(PacketEntity entry) {
            return super.remove(entry)
                .doOnNext(PacketEntity::remove);
        }

        @Override
        public boolean shouldTick() {
            return this.isLoaded();
        }

        @Override
        public void shouldTick(boolean should) {
            this.setLoaded(should);
        }
    }
}
