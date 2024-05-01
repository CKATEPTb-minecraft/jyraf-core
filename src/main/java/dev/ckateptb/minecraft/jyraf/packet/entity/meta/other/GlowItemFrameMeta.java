package dev.ckateptb.minecraft.jyraf.packet.entity.meta.other;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class GlowItemFrameMeta extends ItemFrameMeta {

    public static final byte OFFSET = ItemFrameMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public GlowItemFrameMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
