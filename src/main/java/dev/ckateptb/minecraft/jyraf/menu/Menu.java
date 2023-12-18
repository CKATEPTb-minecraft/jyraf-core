package dev.ckateptb.minecraft.jyraf.menu;

import dev.ckateptb.minecraft.jyraf.menu.builder.MenuBuilder;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public interface Menu extends InventoryHolder {
    boolean isClosable();

    boolean isEditable();

    void setClosable(boolean closable);

    void setEditable(boolean editable);

    void open(Player target);

    void close();

    int getSize();

    void setCloseHandler(CloseHandler handler);

    void onClick(InventoryClickEvent event);

    @Nullable CloseHandler getCloseHandler();

    interface CloseHandler {
        void handle(InventoryCloseEvent event);
    }

    interface ClickHandler {
        void handle(InventoryClickEvent event);
    }

    static MenuBuilder builder() {
        return new MenuBuilder();
    }

    Frame[] getFrames();

    void invalidate();
}
