package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.passive;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.AgeableMeta;

public class ChickenMeta extends AgeableMeta {

    public static final byte OFFSET = AgeableMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public ChickenMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
