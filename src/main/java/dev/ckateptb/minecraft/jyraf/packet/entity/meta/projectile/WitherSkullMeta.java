package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ProjectileMeta;

public class WitherSkullMeta extends EntityMeta implements ObjectData, ProjectileMeta {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private int shooter = -1;

    public WitherSkullMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }


    public boolean isInvulnerable() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setInvulnerable(boolean value) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }


    @Override
    public int getObjectData() {
        return this.shooter == -1 ? 0 : this.shooter;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

    @Override
    public int getShooter() {
        return shooter;
    }

    @Override
    public void setShooter(int entityId) {
        this.shooter = entityId;
    }
}
