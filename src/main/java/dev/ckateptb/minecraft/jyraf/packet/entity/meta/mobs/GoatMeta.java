package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.AgeableMeta;

public class GoatMeta extends AgeableMeta {

    public static final byte OFFSET = AgeableMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;


    public GoatMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public boolean isScreaming() {
        return metadata.getIndex(OFFSET, false);
    }

    public void setScreaming(boolean screaming) {
        metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, screaming);
    }

}
