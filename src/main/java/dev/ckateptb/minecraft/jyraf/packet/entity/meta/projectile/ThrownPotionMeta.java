package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;

public class ThrownPotionMeta extends ItemContainerMeta {

    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public ThrownPotionMeta(int entityId, Metadata metadata) {
        super(entityId, metadata, ItemStack.EMPTY);
    }


}
