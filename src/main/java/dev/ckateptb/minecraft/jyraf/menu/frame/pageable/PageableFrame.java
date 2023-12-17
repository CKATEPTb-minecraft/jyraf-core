package dev.ckateptb.minecraft.jyraf.menu.frame.pageable;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class PageableFrame implements Frame {
    public int offset;
    public Frame[] children;

    @Override
    public ItemStack render(Menu menu, int slot) {
        return null;
    }
}
