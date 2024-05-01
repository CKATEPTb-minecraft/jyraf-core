package dev.ckateptb.minecraft.jyraf.packet.entity.meta.types;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class WaterMobMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public WaterMobMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
