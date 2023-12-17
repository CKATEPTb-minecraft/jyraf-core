package dev.ckateptb.minecraft.jyraf.example.menu;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.menu.chest.ChestMenu;
import org.bukkit.entity.Player;

@Component
public class MenuExample implements Command {
    @CommandMethod("jyraf debug menu")
    @CommandDescription("Display example menu")
    @CommandPermission("jyraf.command.debug.menu")
    public void help(Player sender) {
        ChestMenu chestMenu = new ChestMenu("&6Example Menu", 4);
        chestMenu.setEditable(false);
        chestMenu.setClosable(false);
        chestMenu.setCloseHandler(event -> sender.sendMessage("Menu closed"));
        chestMenu.open(sender);
    }
}
