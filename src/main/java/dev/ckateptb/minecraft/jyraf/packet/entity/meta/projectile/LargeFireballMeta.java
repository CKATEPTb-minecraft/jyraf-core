package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ProjectileMeta;

public class LargeFireballMeta extends ItemContainerMeta implements ObjectData, ProjectileMeta {
    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    private int shooterId = -1;

    public LargeFireballMeta(int entityId, Metadata meta) {
        super(entityId, meta, ItemStack.EMPTY);
    }

    @Override
    public int getObjectData() {
        return this.shooterId == -1 ? 0 : this.shooterId;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

    @Override
    public int getShooter() {
        return shooterId;
    }

    @Override
    public void setShooter(int entityId) {
        this.shooterId = entityId;
    }

}
