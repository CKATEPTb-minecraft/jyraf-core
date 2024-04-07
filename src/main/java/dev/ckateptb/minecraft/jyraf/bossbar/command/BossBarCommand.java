package dev.ckateptb.minecraft.jyraf.bossbar.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.bossbar.BossBar;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@Component
@RequiredArgsConstructor
public class BossBarCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafbossbar <color> <style> <progress> <title>")
    @CommandPermission("jbars.admin")
    public void block(
            Player player,
            @Argument("color") net.kyori.adventure.bossbar.BossBar.Color color,
            @Argument("style") net.kyori.adventure.bossbar.BossBar.Overlay overlay,
            @Argument("progress") int progress,
            @Argument("title") String title
    ) {
        Location location = player.getLocation();
        Bukkit.getScheduler().runTask(Jyraf.getPlugin(), () -> location.getBlock().setType(Material.BEDROCK));
        BossBar bossBar = new BossBar.Builder(location, title)
                .color(color)
                .overlay(overlay)
                .progress(progress)
                .build();
        this.service.getRepository(BossBar.class, location.getWorld())
                .flatMap(worldRepository -> worldRepository.add(bossBar))
                .subscribe();
    }
}
