package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster.raider;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class IllusionerMeta extends SpellcasterIllagerMeta {

    public static final byte OFFSET = SpellcasterIllagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public IllusionerMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
