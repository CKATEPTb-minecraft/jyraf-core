package dev.ckateptb.minecraft.jyraf.packet.entity.meta.other;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class LeashKnotMeta extends EntityMeta {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public LeashKnotMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
