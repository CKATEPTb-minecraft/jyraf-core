package dev.ckateptb.minecraft.jyraf.packet.entity.meta.projectile;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ProjectileMeta;

public class DragonFireballMeta extends EntityMeta implements ProjectileMeta, ObjectData {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    private int shooter = -1;

    public DragonFireballMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
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
