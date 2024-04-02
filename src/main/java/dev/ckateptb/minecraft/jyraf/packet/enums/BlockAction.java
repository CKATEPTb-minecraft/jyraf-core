package dev.ckateptb.minecraft.jyraf.packet.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlockAction {

    OPEN(1, 1),
    CLOSE(1, 0);

    private final int id;
    private final int paramId;

}