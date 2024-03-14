package dev.ckateptb.minecraft.jyraf.packet.entity.enums.villager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum VillagerType {
    DESERT(0),
    JUNGLE(1),
    PLAINS(2),
    SAVANNA(3),
    SNOW(4),
    SWAMP(5),
    TAIGA(6);
    private final int id;
}