package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class GiantMeta extends MobMeta {
    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public GiantMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
