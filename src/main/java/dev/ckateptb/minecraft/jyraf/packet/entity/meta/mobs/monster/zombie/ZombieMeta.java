package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.zombie;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class ZombieMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public ZombieMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public boolean isBaby() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }

    public boolean isBecomingDrowned() {
        return super.metadata.getIndex(offset(OFFSET, 2), false);
    }

    public void setBecomingDrowned(boolean value) {
        super.metadata.setIndex(offset(OFFSET, 2), EntityDataTypes.BOOLEAN, value);
    }

}
