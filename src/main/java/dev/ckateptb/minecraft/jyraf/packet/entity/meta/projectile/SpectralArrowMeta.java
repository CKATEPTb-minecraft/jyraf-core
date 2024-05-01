package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ProjectileMeta;

public class SpectralArrowMeta extends BaseArrowMeta implements ProjectileMeta, ObjectData {

    public static final byte OFFSET = BaseArrowMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    private int shooterId = -1;

    public SpectralArrowMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    @Override
    public int getObjectData() {
        return this.shooterId == -1 ? 0 : this.shooterId + 1;
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
