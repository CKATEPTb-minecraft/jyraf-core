package dev.ckateptb.minecraft.jyraf.menu.frame;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Frame {
    ItemStack render(Menu menu, int slot);

    Menu getMenu();

    void setMenu(Menu menu);

    interface Clickable {
        void onClick(InventoryClickEvent event);
    }
}
