package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.cuboid;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class MagmaCubeMeta extends SlimeMeta {

    public static final byte OFFSET = SlimeMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public MagmaCubeMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
