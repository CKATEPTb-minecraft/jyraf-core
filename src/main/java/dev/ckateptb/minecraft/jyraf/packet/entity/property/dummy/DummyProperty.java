package dev.ckateptb.minecraft.jyraf.packet.entity.property.dummy;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class DummyProperty<T> extends PacketEntityProperty<T> {
    public DummyProperty(String name, T defaultValue) {
        this(name, defaultValue, true);
    }

    public DummyProperty(String name, Class<T> clazz) {
        this(name, clazz, true);
    }

    @SuppressWarnings("unchecked")
    public DummyProperty(String name, T defaultValue, boolean playerModifiable) {
        super(name, defaultValue, (Class<T>) defaultValue.getClass());
        setPlayerModifiable(playerModifiable);
    }

    public DummyProperty(String name, Class<T> clazz, boolean playerModifiable) {
        super(name, null, clazz);
        setPlayerModifiable(playerModifiable);
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
    }
}
