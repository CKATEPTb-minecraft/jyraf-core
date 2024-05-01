package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class ElderGuardianMeta extends GuardianMeta {

    public static final byte OFFSET = GuardianMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public ElderGuardianMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
