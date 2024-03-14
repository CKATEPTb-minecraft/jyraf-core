package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import dev.ckateptb.minecraft.jyraf.packet.entity.skin.cache.MojangSkinCache;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.FetchingDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.MirrorDescriptor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.descriptor.PrefetchedDescriptor;

import java.net.MalformedURLException;
import java.net.URL;

public class SkinDescriptorFactory {
    private final MojangSkinCache skinCache;
    private final MirrorDescriptor mirrorDescriptor;

    public SkinDescriptorFactory(MojangSkinCache skinCache) {
        this.skinCache = skinCache;
        mirrorDescriptor = new MirrorDescriptor(skinCache);
    }

    public SkinDescriptor createMirrorDescriptor() {
        return mirrorDescriptor;
    }

    public SkinDescriptor createRefreshingDescriptor(String playerName) {
        return new FetchingDescriptor(skinCache, playerName);
    }

    public SkinDescriptor createStaticDescriptor(String playerName) {
        return PrefetchedDescriptor.forPlayer(skinCache, playerName).join();
    }

    public SkinDescriptor createStaticDescriptor(String texture, String signature) {
        return new PrefetchedDescriptor(new Skin(texture, signature));
    }

    public SkinDescriptor createUrlDescriptor(String url, String variant) {
        try {
            return createUrlDescriptor(new URL(url), variant);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public SkinDescriptor createUrlDescriptor(URL url, String variant) {
        return PrefetchedDescriptor.fromUrl(skinCache, url, variant).join();
    }
}
