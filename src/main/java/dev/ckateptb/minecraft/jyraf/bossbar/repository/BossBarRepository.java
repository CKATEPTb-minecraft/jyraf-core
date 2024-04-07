package dev.ckateptb.minecraft.jyraf.bossbar.repository;

import dev.ckateptb.minecraft.jyraf.bossbar.BossBar;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.AbstractWorldRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class BossBarRepository extends AbstractWorldRepository<UUID, BossBar> implements Repository.Tickable {

    public BossBarRepository(World world) {
        super(world);
    }

    @Override
    public void tick() {
        this.getChunks()
                .cast(BossBarChunkRepository.class)
                .filter(BossBarChunkRepository::shouldTick)
                .subscribe(BossBarChunkRepository::tick);
    }

    @Override
    protected boolean isValid(BossBar entry) {
        return this.world.getUID().equals(entry.getWorld().getUID());
    }

    @Override
    protected UUID getKey(BossBar entry) {
        return entry.getUid();
    }

    @Override
    protected long getChunkKey(BossBar entry) {
        return Chunk.getChunkKey(entry.getLocation());
    }

    @Override
    protected ChunkRepository<BossBar> createChunkRepository(Long chunkKey) {
        return new BossBarChunkRepository(chunkKey);
    }

    @Override
    public boolean shouldTick() {
        return true;
    }

    @Override
    public void shouldTick(boolean should) {

    }

    public static class BossBarChunkRepository extends AbstractChunkRepository<UUID, BossBar> implements Tickable {

        public BossBarChunkRepository(Long chunkKey) {
            super(chunkKey);
        }

        @Override
        protected UUID getKey(BossBar entry) {
            return entry.getUid();
        }

        @Override
        public void tick() {
            this.get()
                    .subscribe(BossBar::tick);
        }

        @Override
        public Mono<BossBar> add(BossBar entry) {
            return Mono.justOrEmpty(this.entries.getIfPresent(entry.getUid()))
                    .flatMap(Mono::fromFuture)
                    .map(this::remove)
                    .flatMap(ignored -> Mono.empty())
                    .switchIfEmpty(Mono.defer(() -> super.add(entry)))
                    .cast(BossBar.class);
        }

        @Override
        public Mono<BossBar> remove(BossBar entry) {
            return super.remove(entry)
                    .doOnNext(BossBar::remove);
        }

        @Override
        protected boolean isValid(BossBar entry) {
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