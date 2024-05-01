package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.water;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.WaterMobMeta;

public class SquidMeta extends WaterMobMeta {

    public static final byte OFFSET = WaterMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public SquidMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
