package dev.ckateptb.minecraft.jyraf.menu.chest;

import dev.ckateptb.minecraft.jyraf.component.Component;
import dev.ckateptb.minecraft.jyraf.menu.AbstractMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

@Setter
@Getter
public class ChestMenu extends AbstractMenu {
    private final Inventory inventory;
    private final Frame[] frames;

    public ChestMenu(String title, int rows) {
        Validate.notBlank(title, "Title can't be null");
        Validate.inclusiveBetween(1, 6, rows, "Rows must be from 1 to 6! ");
        int size = rows * 9;
        this.inventory = Bukkit.createInventory(this, size, Component.of(title));
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

    public void setFrame(int slot, Frame frame) {
        this.frames[slot] = frame;
        this.inventory.setItem(slot, frame.render(this, slot));
    }

    public void clear() {
        for (int i = 0; i < frames.length; i++) {
            this.setFrame(i, null);
        }
    }
}
