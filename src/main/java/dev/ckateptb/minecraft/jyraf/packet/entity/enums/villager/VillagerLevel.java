package dev.ckateptb.minecraft.jyraf.packet.entity.enums.villager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum VillagerLevel {
    STONE,
    IRON,
    GOLD,
    EMERALD,
    DIAMOND
}
