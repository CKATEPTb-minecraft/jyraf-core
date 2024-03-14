package dev.ckateptb.minecraft.jyraf.packet.entity.property.equipment;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class EquipmentProperty extends PacketEntityProperty<ItemStack> {
    private final EquipmentSlot slot;

    public EquipmentProperty(String name, EquipmentSlot slot) {
        super(name, null, ItemStack.class);
        this.slot = slot;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        entity.equip(player, new Equipment(slot, entity.getProperty(this)));
    }
}
