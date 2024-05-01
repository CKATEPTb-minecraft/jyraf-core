package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.horse;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class MuleMeta extends ChestedHorseMeta {

    public static final byte OFFSET = ChestedHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public MuleMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
