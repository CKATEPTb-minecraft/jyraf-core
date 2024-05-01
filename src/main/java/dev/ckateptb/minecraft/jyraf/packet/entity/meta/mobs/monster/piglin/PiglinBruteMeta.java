package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.piglin;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class PiglinBruteMeta extends BasePiglinMeta {

    public static final byte OFFSET = BasePiglinMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public PiglinBruteMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
