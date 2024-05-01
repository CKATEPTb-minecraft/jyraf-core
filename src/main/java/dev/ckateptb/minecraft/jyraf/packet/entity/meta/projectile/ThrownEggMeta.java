package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;

public class ThrownEggMeta extends ItemContainerMeta {


    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    private static final ItemStack EGG = ItemStack.builder().type(ItemTypes.EGG).build();

    public ThrownEggMeta(int entityId, Metadata metadata) {
        super(entityId, metadata, EGG);
    }

}
