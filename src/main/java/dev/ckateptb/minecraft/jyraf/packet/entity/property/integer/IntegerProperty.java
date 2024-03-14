package dev.ckateptb.minecraft.jyraf.packet.entity.property.integer;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class IntegerProperty extends PacketEntityProperty<Integer> {
    private final int index;
    private final boolean legacy;

    public IntegerProperty(String name, int index, Integer defaultValue) {
        this(name, index, defaultValue, false);
    }

    public IntegerProperty(String name, int index, Integer defaultValue, boolean legacy) {
        super(name, defaultValue, Integer.class);
        this.index = index;
        this.legacy = legacy;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        properties.put(index, legacy ?
                newEntityData(index, EntityDataTypes.BYTE, (byte) entity.getProperty(this).intValue()) :
                newEntityData(index, EntityDataTypes.INT, entity.getProperty(this)));
    }
}
