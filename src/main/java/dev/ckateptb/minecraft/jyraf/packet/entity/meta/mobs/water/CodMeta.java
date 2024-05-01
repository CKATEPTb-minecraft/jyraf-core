package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.water;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class CodMeta extends BaseFishMeta {

    public static final byte OFFSET = BaseFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public CodMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
