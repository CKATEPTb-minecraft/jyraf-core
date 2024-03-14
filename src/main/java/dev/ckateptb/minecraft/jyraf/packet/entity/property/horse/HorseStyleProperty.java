package dev.ckateptb.minecraft.jyraf.packet.entity.property.horse;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.horse.HorseStyle;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class HorseStyleProperty extends PacketEntityProperty<HorseStyle> {
    private final int index;

    public HorseStyleProperty(int index) {
        super("horse_style", HorseStyle.NONE, HorseStyle.class);
        this.index = index;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        EntityData oldData = properties.get(index);
        HorseStyle value = entity.getProperty(this);
        properties.put(index, newEntityData(index, EntityDataTypes.INT, (oldData == null ? 0 : ((int) oldData.getValue() & 0x00FF)) | (value.ordinal() << 8)));
    }
}