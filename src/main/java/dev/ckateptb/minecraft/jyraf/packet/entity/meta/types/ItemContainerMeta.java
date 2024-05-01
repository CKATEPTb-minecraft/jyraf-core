package dev.ckateptb.minecraft.jyraf.packet.entity.meta.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public abstract class ItemContainerMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final ItemStack baseItem;

    protected ItemContainerMeta(int entityId, Metadata metadata, ItemStack baseItem) {
        super(entityId, metadata);
        this.baseItem = baseItem;
    }

    public ItemStack getItem() {
        return super.metadata.getIndex(OFFSET, baseItem);
    }

    public void setItem(ItemStack value) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.ITEMSTACK, value);
    }

}
