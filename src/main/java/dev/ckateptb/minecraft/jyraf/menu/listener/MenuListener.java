package dev.ckateptb.minecraft.jyraf.menu.listener;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MenuListener implements Listener {
    private final Jyraf plugin;

    @EventHandler
    public void on(InventoryClickEvent event) {
        this.findMenu(event).ifPresent(menu -> {
            if (!menu.isEditable()) {
                event.setCancelled(true);
            }
            menu.onClick(event);
        });
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        this.findMenu(event).ifPresent(menu -> {
            Menu.CloseHandler closeHandler = menu.getCloseHandler();
            if (closeHandler != null) closeHandler.handle(event);
            if (!menu.isClosable() && event.getPlayer() instanceof Player player) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> menu.open(player), 0);
            }
        });
    }

    public Optional<Menu> findMenu(InventoryEvent event) {
        return Optional.ofNullable(event.getInventory().getHolder())
                .filter(holder -> holder instanceof Menu)
                .map(holder -> (Menu) holder);
    }
}
