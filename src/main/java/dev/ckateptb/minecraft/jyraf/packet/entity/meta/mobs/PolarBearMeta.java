package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.AgeableMeta;

public class PolarBearMeta extends AgeableMeta {

    public static final byte OFFSET = AgeableMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public PolarBearMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public boolean isStandingUp() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setStandingUp(boolean value) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }

}
