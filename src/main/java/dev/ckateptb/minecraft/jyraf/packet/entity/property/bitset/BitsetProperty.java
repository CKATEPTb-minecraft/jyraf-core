package dev.ckateptb.minecraft.jyraf.packet.entity.property.bitset;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class BitsetProperty extends PacketEntityProperty<Boolean> {
    private final int index;
    private final int bitmask;
    private final boolean inverted;
    private boolean integer = false;

    public BitsetProperty(String name, int index, int bitmask, boolean inverted, boolean integer) {
        this(name, index, bitmask, inverted);
        this.integer = integer;
    }

    public BitsetProperty(String name, int index, int bitmask, boolean inverted) {
        super(name, inverted, Boolean.class);
        this.index = index;
        this.bitmask = bitmask;
        this.inverted = inverted;
    }

    public BitsetProperty(String name, int index, int bitmask) {
        this(name, index, bitmask, false);
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        EntityData oldData = properties.get(this.index);
        boolean enabled = entity.getProperty(this);
        if (this.inverted) enabled = !enabled;
        properties.put(this.index,
                this.integer ? newEntityData(this.index, EntityDataTypes.INT, (oldData == null ? 0 : (int) oldData.getValue()) | (enabled ? this.bitmask : 0)) :
                        newEntityData(this.index, EntityDataTypes.BYTE, (byte) ((oldData == null ? 0 : (byte) oldData.getValue()) | (enabled ? this.bitmask : 0))));

    }
}
