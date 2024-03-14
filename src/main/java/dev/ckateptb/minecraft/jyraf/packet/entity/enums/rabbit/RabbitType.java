package dev.ckateptb.minecraft.jyraf.packet.entity.enums.rabbit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum RabbitType {
    BROWN(0),
    WHITE(1),
    BLACK(2),
    BLACK_AND_WHITE(3),
    GOLD(4),
    SALT_AND_PEPPER(5),
    THE_KILLER_BUNNY(99),
    TOAST(100);

    private final int id;
}