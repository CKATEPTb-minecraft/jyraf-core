package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;

public class EyeOfEnderMeta extends ItemContainerMeta {


    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public static final ItemStack EYE_OF_ENDER = ItemStack.builder().type(ItemTypes.ENDER_EYE).build();

    public EyeOfEnderMeta(int entityId, Metadata metadata) {
        super(entityId, metadata, EYE_OF_ENDER);
    }

}
