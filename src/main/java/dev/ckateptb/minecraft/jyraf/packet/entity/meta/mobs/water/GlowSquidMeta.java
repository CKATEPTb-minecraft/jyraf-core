package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.water;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class GlowSquidMeta extends SquidMeta {

    public static final byte OFFSET = SquidMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public GlowSquidMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public int getDarkTicksRemaining() {
        return metadata.getIndex(OFFSET, 0);
    }

    public void setDarkTicksRemaining(int ticks) {
        metadata.setIndex(OFFSET, EntityDataTypes.INT, ticks);
    }


}
