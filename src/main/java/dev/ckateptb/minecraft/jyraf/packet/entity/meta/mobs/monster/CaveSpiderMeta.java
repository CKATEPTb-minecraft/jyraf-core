package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class CaveSpiderMeta extends SpiderMeta {

    public static final byte OFFSET = SpiderMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public CaveSpiderMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
