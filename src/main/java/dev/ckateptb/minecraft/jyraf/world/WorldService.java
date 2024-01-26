package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import org.bukkit.World;

@Component
public class WorldService {
    private final AsyncCache<World, WorldRepository> world = Caffeine.newBuilder().buildAsync();
}
