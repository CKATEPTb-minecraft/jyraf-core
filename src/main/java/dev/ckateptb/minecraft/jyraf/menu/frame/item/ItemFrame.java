package dev.ckateptb.minecraft.jyraf.menu.frame.item;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class ItemFrame implements Frame {
    protected ItemStack item;

    @Override
    public ItemStack render(Menu menu, int slot) {
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
