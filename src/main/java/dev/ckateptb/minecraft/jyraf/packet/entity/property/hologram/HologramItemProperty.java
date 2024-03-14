package dev.ckateptb.minecraft.jyraf.packet.entity.property.hologram;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class HologramItemProperty extends PacketEntityProperty<ItemStack> {

    public HologramItemProperty() {
        super("holo_item", null, ItemStack.class);
        setPlayerModifiable(false);
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        properties.put(8, newEntityData(8, EntityDataTypes.ITEMSTACK, entity.getProperty(this)));
        properties.put(5, newEntityData(5, EntityDataTypes.BOOLEAN, true));
    }
}
