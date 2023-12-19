package dev.ckateptb.minecraft.jyraf.menu.frame.item;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

@Getter
@Setter
public class ItemFrame implements Frame, Frame.Invalidable {
    protected ItemStack item;
    private Menu menu;
    private BiConsumer<Menu, Integer> invalidate;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        return this.item;
    }

    @Override
    public void invalidate(Menu menu, int slot) {
        if (invalidate != null) {
            invalidate.accept(menu, slot);
        }
    }

    @Override
    public ItemFrame invalidate(BiConsumer<Menu, Integer> invalidate) {
        this.invalidate = invalidate;
        return this;
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
