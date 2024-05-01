package dev.ckateptb.minecraft.jyraf.packet.entity.meta.other;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;

public class LlamaSpitMeta extends EntityMeta implements ObjectData {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public LlamaSpitMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }


    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }
}
