package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.horse;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class SkeletonHorseMeta extends BaseHorseMeta {

    public static final byte OFFSET = BaseHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public SkeletonHorseMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
