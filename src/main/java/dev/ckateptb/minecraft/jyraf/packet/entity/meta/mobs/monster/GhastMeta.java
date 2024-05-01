package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class GhastMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public GhastMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }


    public boolean isAttacking() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setAttacking(boolean value) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }

}
