package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.raider;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class RavagerMeta extends RaiderMeta {

    public static final byte OFFSET = RaiderMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;


    public RavagerMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
