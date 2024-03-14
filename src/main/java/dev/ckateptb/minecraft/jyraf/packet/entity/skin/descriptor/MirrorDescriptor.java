package dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor;

import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.SkinDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.cache.MojangSkinCache;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class MirrorDescriptor implements SkinDescriptor {
    private final MojangSkinCache skinCache;

    public MirrorDescriptor(MojangSkinCache skinCache) {
        this.skinCache = skinCache;
    }

    @Override
    public CompletableFuture<Skin> fetch(Player player) {
        return CompletableFuture.completedFuture(skinCache.getFromPlayer(player));
    }

    @Override
    public Skin fetchInstant(Player player) {
        return skinCache.getFromPlayer(player);
    }

    @Override
    public boolean supportsInstant(Player player) {
        return true;
    }

    @Override
    public String serialize() {
        return "mirror";
    }
}
