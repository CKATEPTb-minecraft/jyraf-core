package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.world.chunk.ChunkRepository;

public class WorldRepository {
    private final AsyncCache<Long, ChunkRepository> chunks = Caffeine.newBuilder().buildAsync();
}
