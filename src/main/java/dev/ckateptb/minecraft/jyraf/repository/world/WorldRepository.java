package dev.ckateptb.minecraft.jyraf.repository.world;

import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorldRepository<T> extends Repository<T> {
    Mono<ChunkRepository<T>> getChunk(Long chunkKey);

    Flux<ChunkRepository<T>> getChunks();

    World getWorld();

    default Flux<ChunkRepository<T>> getNearbyChunks(Location location, double xRadius, double zRadius) {
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
