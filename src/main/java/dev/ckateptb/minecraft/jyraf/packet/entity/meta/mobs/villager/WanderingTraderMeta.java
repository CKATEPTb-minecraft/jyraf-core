package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.villager;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;

public class WanderingTraderMeta extends VillagerMeta {

    public static final byte OFFSET = VillagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET;

    public WanderingTraderMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }
}
