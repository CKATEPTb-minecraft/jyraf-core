package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.skeleton;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class WitherSkeletonMeta extends SkeletonMeta {
    public static final byte OFFSET = SkeletonMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public WitherSkeletonMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
