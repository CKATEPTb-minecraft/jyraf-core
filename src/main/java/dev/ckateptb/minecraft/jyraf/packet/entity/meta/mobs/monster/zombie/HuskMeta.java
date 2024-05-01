package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.zombie;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class HuskMeta extends ZombieMeta {

    public static final byte OFFSET = ZombieMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public HuskMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
