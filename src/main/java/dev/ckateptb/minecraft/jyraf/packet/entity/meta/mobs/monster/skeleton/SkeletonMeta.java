package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.skeleton;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class SkeletonMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public SkeletonMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
