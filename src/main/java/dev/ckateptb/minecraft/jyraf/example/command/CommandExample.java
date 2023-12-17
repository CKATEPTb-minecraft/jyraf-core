package dev.ckateptb.minecraft.jyraf.example.command;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.environment.Environment;
import dev.ckateptb.minecraft.jyraf.example.config.ConfigExample;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CommandExample implements Command {
    private final Jyraf plugin;
    private final ConfigExample config;

    @CommandMethod("jyraf")
    @CommandDescription("Display config example")
    @CommandPermission("jyraf.command.example")
    public void help(CommandSender sender) {
        Stream.of(
                        "&7/jyraf reload - reload config file",
                        "&8Debug: " + config.getDebug(),
                        "&8PaperAPI: " + Environment.PAPER.check(),
                        "&8SpigotAPI: " + Environment.SPIGOT.check(),
                        "&8BukkitAPI: " + Environment.BUKKIT.check()
                )
                .forEach(text -> sender.sendMessage(Text.of(text)));
    }

    @CommandMethod("jyraf reload|r")
    @CommandDescription("Reload configuration file")
    @CommandPermission("jyraf.command.reload")
    public void reload(CommandSender sender) {
        config.load();
        String text = String.format("&7%s - &6%s", plugin.getName(), plugin.getDescription().getVersion());
        sender.sendMessage(Text.of(text));
    }
}
