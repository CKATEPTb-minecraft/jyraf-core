package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;

public class ItemEntityMeta extends ItemContainerMeta implements ObjectData {

    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    protected ItemEntityMeta(int entityId, Metadata metadata) {
        super(entityId, metadata, ItemStack.EMPTY);
    }

    @Override
    public int getObjectData() {
        return 1;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }
}
