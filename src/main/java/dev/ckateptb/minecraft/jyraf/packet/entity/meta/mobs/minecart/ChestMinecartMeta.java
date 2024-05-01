package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.minecart;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class ChestMinecartMeta extends BaseMinecartMeta {

    public static final byte OFFSET = BaseMinecartMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;


    public ChestMinecartMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    @Override
    public int getObjectData() {
        return 1;
    }
}
