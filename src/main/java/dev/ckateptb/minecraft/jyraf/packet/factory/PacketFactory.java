package dev.ckateptb.minecraft.jyraf.packet.factory;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_17.V1_17PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_19_3.V1_19_3PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_20_2.V1_20_2PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.factory.v1_8.V1_8PacketFactory;
import lombok.experimental.Delegate;

import java.util.HashMap;

@Component
public class PacketFactory {
    public static final CachedReference<PacketFactory> INSTANCE = new CachedReference<>();
    private final Jyraf plugin;
    @Delegate
    private final V1_8PacketFactory delegate;

    public PacketFactory(Jyraf plugin) {
        this.plugin = plugin;
        this.delegate = getPacketFactory();
        INSTANCE.set(this);
    }

    private V1_8PacketFactory getPacketFactory() {
        if(this.delegate != null) return this.delegate;
        HashMap<ServerVersion, CachedReference<? extends V1_8PacketFactory>> versions = new HashMap<>();
        versions.put(ServerVersion.V_1_16_5, new CachedReference<>(V1_8PacketFactory::new));
        versions.put(ServerVersion.V_1_17, new CachedReference<>(V1_17PacketFactory::new));
        versions.put(ServerVersion.V_1_19_3, new CachedReference<>(V1_19_3PacketFactory::new));
        versions.put(ServerVersion.V_1_20_2, new CachedReference<>(V1_20_2PacketFactory::new));
        ServerVersion version = this.plugin.getPacketApi().getServerManager().getVersion();
        if (versions.containsKey(version)) return versions.get(version).force();
        for (ServerVersion v : ServerVersion.reversedValues()) {
            if (v.isNewerThan(version)) continue;
            if (!versions.containsKey(v)) continue;
            return versions.get(v).force();
        }
        throw new RuntimeException("Unsupported version!");
    }
}
