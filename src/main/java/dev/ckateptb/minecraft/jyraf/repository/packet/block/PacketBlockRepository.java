package dev.ckateptb.minecraft.jyraf.repository.packet.block;

import com.github.retrooper.packetevents.util.Vector3i;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.AbstractWorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Mono;

public class PacketBlockRepository extends AbstractWorldRepository<Vector3i, PacketBlock> implements Repository.Tickable {

    public PacketBlockRepository(World world) {
        super(world);
    }

    @Override
    public void tick() {
        this.getChunks()
                .cast(PacketBlockChunkRepository.class)
                .filter(PacketBlockChunkRepository::shouldTick)
                .subscribe(PacketBlockChunkRepository::tick);
    }

    @Override
    protected boolean isValid(PacketBlock entry) {
        return this.world.getUID().equals(entry.getWorld().getUID());
    }

    @Override
    protected Vector3i getKey(PacketBlock entry) {
        return entry.getPosition();
    }

    @Override
    protected long getChunkKey(PacketBlock entry) {
        return Chunk.getChunkKey(entry.getLocation());
    }

    @Override
    protected ChunkRepository<PacketBlock> createChunkRepository(Long chunkKey) {
        return new PacketBlockChunkRepository(chunkKey);
    }

    @Override
    public boolean shouldTick() {
        return true;
    }

    @Override
    public void shouldTick(boolean should) {

    }

    public static class PacketBlockChunkRepository extends AbstractChunkRepository<Vector3i, PacketBlock> implements Tickable {

        public PacketBlockChunkRepository(Long chunkKey) {
            super(chunkKey);
        }

        @Override
        protected Vector3i getKey(PacketBlock entry) {
            return entry.getPosition();
        }

        @Override
        public void tick() {
            this.get()
                    .subscribe(PacketBlock::tick);
        }

        @Override
        public Mono<PacketBlock> add(PacketBlock entry) {
            return Mono.justOrEmpty(this.entries.getIfPresent(entry.getPosition()))
                    .flatMap(Mono::fromFuture)
                    .map(this::remove)
                    .flatMap(ignored -> Mono.empty())
                    .switchIfEmpty(Mono.defer(() -> super.add(entry)))
                    .cast(PacketBlock.class);
        }

        @Override
        public Mono<PacketBlock> remove(PacketBlock entry) {
            return super.remove(entry)
                    .doOnNext(PacketBlock::remove);
        }

        @Override
        protected boolean isValid(PacketBlock entry) {
            Location location = entry.getLocation();
            long chunkKey = Chunk.getChunkKey(location);
            return this.chunkKey.equals(chunkKey);
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