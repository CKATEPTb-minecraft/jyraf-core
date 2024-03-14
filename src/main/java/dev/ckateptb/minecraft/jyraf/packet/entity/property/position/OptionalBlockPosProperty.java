package dev.ckateptb.minecraft.jyraf.packet.entity.property.position;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public class OptionalBlockPosProperty extends PacketEntityProperty<Vector3i> {
    private final int index;

    public OptionalBlockPosProperty(String name, Vector3i defaultValue, int index) {
        super(name, defaultValue, Vector3i.class);
        this.index = index;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        Vector3i value = entity.getProperty(this);
        if (value == null) properties.put(index, new EntityData(index, EntityDataTypes.OPTIONAL_BLOCK_POSITION, Optional.empty()));
        else properties.put(index, new EntityData(index, EntityDataTypes.OPTIONAL_BLOCK_POSITION,
                Optional.of(new com.github.retrooper.packetevents.util.Vector3i(value.getX(), value.getY(), value.getZ()))));
    }
}