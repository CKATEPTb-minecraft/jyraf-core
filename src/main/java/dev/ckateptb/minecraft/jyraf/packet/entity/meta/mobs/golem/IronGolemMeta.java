package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.golem;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class IronGolemMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final static byte PLAYER_CREATED_BIT = 0x01;

    public IronGolemMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public boolean isPlayerCreated() {
        return getMaskBit(OFFSET, PLAYER_CREATED_BIT);
    }

    public void setPlayerCreated(boolean value) {
        setMaskBit(OFFSET, PLAYER_CREATED_BIT, value);
    }


}
