package dev.ckateptb.minecraft.jyraf.menu.chest;

import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.menu.AbstractMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class ChestMenu extends AbstractMenu {
    private final Inventory inventory;
    private final Frame[] frames;

    public ChestMenu(String title, int rows) {
        Validate.notBlank(title, "Title can't be null");
        Validate.inclusiveBetween(1, 6, rows, "Rows must be from 1 to 6! ");
        int size = rows * 9;
        this.inventory = Bukkit.createInventory(this, size, Text.of(title));
        this.frames = new Frame[size];
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory != this.inventory) return;
        int slot = event.getSlot();
        if (slot >= frames.length) return;
        Frame frame = frames[slot];
        if (frame instanceof Frame.Clickable clickable) {
            clickable.onClick(event);
        }
    }

    @Override
    public void invalidate() {
        for (int i = 0; i < frames.length; i++) {
            Frame frame = frames[i];
            this.setFrame(i, frame);
            if (frame instanceof Frame.Invalidable invalidable) {
                invalidable.invalidate(this, i);
            }
        }
    }

    public void setFrame(int slot, @Nullable Frame frame) {
        this.frames[slot] = frame;
        this.inventory.setItem(slot, frame == null ? null : frame.render(this, slot));
    }

    public void clear() {
        for (int i = 0; i < frames.length; i++) {
            this.setFrame(i, null);
        }
    }
}
