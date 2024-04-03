package dev.ckateptb.minecraft.jyraf.packet.bossbar.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Action {
    ADD(0),
    REMOVE(1),
    UPDATE_PROGRESS(2),
    UPDATE_TITLE(3),
    UPDATE_STYLE(4),
    UPDATE_FLAGS(5);

    private final int id;
}