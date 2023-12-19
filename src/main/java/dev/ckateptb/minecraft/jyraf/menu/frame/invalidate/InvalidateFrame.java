package dev.ckateptb.minecraft.jyraf.menu.frame.invalidate;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;

import java.util.function.BiFunction;

public interface InvalidateFrame extends Frame {
    BiFunction<Menu, Integer, ? extends Frame> getInvalidate();
    void setInvalidate(BiFunction<Menu, Integer, ? extends Frame> invalidate);
}
