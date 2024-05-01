package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ItemContainerMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ProjectileMeta;

public class SmallFireballMeta extends ItemContainerMeta implements ObjectData, ProjectileMeta {


    public static final byte OFFSET = ItemContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public static final ItemStack SMALL_FIREBALL = ItemStack.builder().type(ItemTypes.FIRE_CHARGE).build();

    private int shooterId = -1;

    public SmallFireballMeta(int entityId, Metadata meta) {
        super(entityId, meta, SMALL_FIREBALL);
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
