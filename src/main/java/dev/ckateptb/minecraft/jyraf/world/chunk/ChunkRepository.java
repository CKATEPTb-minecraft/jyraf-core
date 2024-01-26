package dev.ckateptb.minecraft.jyraf.world.chunk;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.world.WorldRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

@RequiredArgsConstructor
public class ChunkRepository {
    private final AsyncCache<Integer, Entity> entities = Caffeine.newBuilder().buildAsync();
    @Getter
    private final WorldRepository world;
    @Getter
    private final Chunk chunk;
}
