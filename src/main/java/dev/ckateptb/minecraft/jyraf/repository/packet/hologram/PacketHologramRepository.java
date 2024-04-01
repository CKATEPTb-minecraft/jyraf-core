package dev.ckateptb.minecraft.jyraf.repository.packet.hologram;

import dev.ckateptb.minecraft.jyraf.packet.hologram.PacketHologram;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.AbstractWorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PacketHologramRepository extends AbstractWorldRepository<UUID, PacketHologram> implements Repository.Tickable {
    public PacketHologramRepository(World world) {
        super(world);
    }

    @Override
    protected boolean isValid(PacketHologram entry) {
        return this.world.getUID().equals(entry.getWorld().getUID());
    }

    @Override
    protected UUID getKey(PacketHologram entry) {
        return entry.getUniqueId();
    }

    @Override
    protected long getChunkKey(PacketHologram entry) {
        return Chunk.getChunkKey(entry.getLocation());
    }

    @Override
    protected ChunkRepository<PacketHologram> createChunkRepository(Long chunkKey) {
        return new PacketHologramChunkRepository(chunkKey);
    }

    @Override
    public void tick() {
        this.getChunks()
                .cast(PacketHologramChunkRepository.class)
                .filter(PacketHologramChunkRepository::shouldTick)
                .doOnNext(PacketHologramChunkRepository::tick)
                .flatMap(Repository::get)
                .filterWhen(hologram -> this.getCachedChunkKey(hologram.getUniqueId())
                        .map(chunkKey -> !chunkKey.equals(Chunk.getChunkKey(hologram.getLocation()))))
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

    public static class PacketHologramChunkRepository extends AbstractChunkRepository<UUID, PacketHologram> implements Tickable {

        public PacketHologramChunkRepository(Long chunkKey) {
            super(chunkKey);
        }

        @Override
        protected UUID getKey(PacketHologram entry) {
            return entry.getUniqueId();
        }

        @Override
        protected boolean isValid(PacketHologram entry) {
            Location location = entry.getLocation();
            long chunkKey = Chunk.getChunkKey(location);
            return this.chunkKey.equals(chunkKey);
        }

        @Override
        public void tick() {
            this.get().subscribe(PacketHologram::tick);
        }

        @Override
        public Mono<PacketHologram> remove(PacketHologram entry) {
            return super.remove(entry)
                    .doOnNext(PacketHologram::remove);
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
