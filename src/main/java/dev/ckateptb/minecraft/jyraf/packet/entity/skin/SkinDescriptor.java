package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.cache.MojangSkinCache;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.FetchingDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.MirrorDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.PrefetchedDescriptor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SkinDescriptor {
    CompletableFuture<Skin> fetch(Player player);

    Skin fetchInstant(Player player);

    boolean supportsInstant(Player player);

    String serialize();

    static SkinDescriptor deserialize(MojangSkinCache skinCache, String str) {
        String[] arr = str.split(";");
        if (arr[0].equalsIgnoreCase("mirror")) return new MirrorDescriptor(skinCache);
        else if (arr[0].equalsIgnoreCase("fetching")) return new FetchingDescriptor(skinCache, arr[1]);
        else if (arr[0].equalsIgnoreCase("prefetched")) {
            List<TextureProperty> properties = new ArrayList<>();
            for (int i = 0; i < (arr.length - 1) / 3; i++) {
                properties.add(new TextureProperty(arr[i + 1], arr[i + 2], arr[i + 3]));
            }
            return new PrefetchedDescriptor(new Skin(properties));
        }
        throw new IllegalArgumentException("Unknown SkinDescriptor type!");
    }
}
