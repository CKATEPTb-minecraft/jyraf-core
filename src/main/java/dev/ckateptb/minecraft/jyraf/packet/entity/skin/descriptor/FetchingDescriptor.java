package dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor;

import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.SkinDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.cache.MojangSkinCache;
import dev.ckateptb.minecraft.jyraf.placeholder.PAPI;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class FetchingDescriptor implements SkinDescriptor {
    private final MojangSkinCache skinCache;
    @Getter
    private final String name;

    public FetchingDescriptor(MojangSkinCache skinCache, String name) {
        this.skinCache = skinCache;
        this.name = name;
    }

    @Override
    public CompletableFuture<Skin> fetch(Player player) {
        return skinCache.fetchByName(PAPI.setPlaceholders(player, name));
    }

    @Override
    public Skin fetchInstant(Player player) {
        return skinCache.getFullyCachedByName(PAPI.setPlaceholders(player, name));
    }

    @Override
    public boolean supportsInstant(Player player) {
        return skinCache.isNameFullyCached(PAPI.setPlaceholders(player, name));
    }

    @Override
    public String serialize() {
        return "fetching;" + name;
    }
}
