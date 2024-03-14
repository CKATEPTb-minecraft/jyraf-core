package dev.ckateptb.minecraft.jyraf.packet.entity.property.rotation;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class RotationProperty extends PacketEntityProperty<Vector3f> {
    private final int index;

    public RotationProperty(String name, int index, Vector3f defaultValue) {
        super(name, defaultValue, Vector3f.class);
        this.index = index;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        Vector3f vec = entity.getProperty(this);
        properties.put(index, newEntityData(index, EntityDataTypes.ROTATION, new com.github.retrooper.packetevents.util.Vector3f(vec.getX(), vec.getY(), vec.getZ())));
    }
}
