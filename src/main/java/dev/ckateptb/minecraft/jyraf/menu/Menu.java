package dev.ckateptb.minecraft.jyraf.menu;

import dev.ckateptb.minecraft.jyraf.menu.builder.MenuBuilder;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public interface Menu extends InventoryHolder {
    static MenuBuilder builder() {
        return new MenuBuilder();
    }

    boolean isClosable();

    void setClosable(boolean closable);

    boolean isEditable();

    void setEditable(boolean editable);

    void open(Player target);

    void close();

    int getSize();

    void onClick(InventoryClickEvent event);

    @Nullable CloseHandler getCloseHandler();

    void setCloseHandler(CloseHandler handler);

    Frame[] getFrames();

    void invalidate();

    interface CloseHandler {
        void handle(InventoryCloseEvent event);
    }

    interface ClickHandler {
        void handle(InventoryClickEvent event);
    }
}
