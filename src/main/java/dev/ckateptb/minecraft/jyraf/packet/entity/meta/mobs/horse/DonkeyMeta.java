package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.horse;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class DonkeyMeta extends ChestedHorseMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public DonkeyMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
