package dev.ckateptb.minecraft.jyraf.packet.enums;

public enum MouseButton {
    RIGHT,
    LEFT;

    public static MouseButton left(boolean left) {
        return left ? LEFT : RIGHT;
    }

    public static MouseButton right(boolean right) {
        return right ? RIGHT : LEFT;
    }

}