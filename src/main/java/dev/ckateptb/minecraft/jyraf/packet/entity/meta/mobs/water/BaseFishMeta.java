package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.water;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.WaterMobMeta;

public class BaseFishMeta extends WaterMobMeta {

    public static final byte OFFSET = WaterMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public BaseFishMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }


    public boolean isFromBucket() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setFromBucket(boolean value) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }

}
