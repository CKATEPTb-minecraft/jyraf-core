package dev.ckateptb.minecraft.jyraf.example.menu;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.menu.chest.ChestMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Component
@RequiredArgsConstructor
public class MenuExample implements Command {
    private final Jyraf plugin;

    @CommandMethod("jyraf debug menu")
    @CommandPermission("jyraf.command.debug.menu")
    public void help(Player sender) {
        ChestMenu chestMenu = new ChestMenu("&6Example Menu", 4);
        chestMenu.setEditable(false);
        chestMenu.setClosable(true);
        chestMenu.setCloseHandler(event -> sender.sendMessage("Menu closed"));
        for (int i = 0; i < 36; i++) {
            chestMenu.setFrame(i, new ItemFrame.Builder()
                    .item(new ItemBuilder(Material.values()[i + 10])
                            .name("Item #" + i + 1)
                            .build())
                    .build());
        }
        plugin.syncScheduler().schedule(() -> chestMenu.open(sender));
        sender.sendMessage(Text.of("&a&[Success](hover:text Hover test!)"));
    }
}
