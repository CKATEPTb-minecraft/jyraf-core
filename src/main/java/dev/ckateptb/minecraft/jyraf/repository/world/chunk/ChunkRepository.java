package dev.ckateptb.minecraft.jyraf.repository.world.chunk;

import dev.ckateptb.minecraft.jyraf.repository.Repository;

public interface ChunkRepository<T> extends Repository<T> {
    Long getChunkKey();

    boolean isLoaded();

    void setLoaded(boolean loaded);
}
