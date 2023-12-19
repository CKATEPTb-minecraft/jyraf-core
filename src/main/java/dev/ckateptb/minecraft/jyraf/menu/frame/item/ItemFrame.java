package dev.ckateptb.minecraft.jyraf.menu.frame.item;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import dev.ckateptb.minecraft.jyraf.menu.frame.invalidate.InvalidateFrame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

@Getter
@Setter
public class ItemFrame implements InvalidateFrame {
    protected ItemStack item;
    private Menu menu;
    private BiFunction<Menu, Integer, ? extends Frame> invalidate;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        return this.item;
    }

    public static class Builder implements dev.ckateptb.minecraft.jyraf.builder.Builder<ItemFrame> {
        protected ItemStack itemStack;

        public Builder item(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public ItemFrame build() {
            ItemFrame frame = new ItemFrame();
            frame.setItem(this.itemStack);
            return frame;
        }
    }
}
