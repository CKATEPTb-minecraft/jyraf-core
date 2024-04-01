package dev.ckateptb.minecraft.jyraf.packet.hologram.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.hologram.PacketHologram;
import dev.ckateptb.minecraft.jyraf.packet.hologram.line.HologramLine;
import dev.ckateptb.minecraft.jyraf.packet.hologram.line.implementation.HologramTextLine;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import reactor.core.scheduler.Schedulers;

import java.util.*;

@Getter
@Component
@RequiredArgsConstructor
public class HologramCommand implements Command {

    private final WorldRepositoryService service;
    private final Set<UUID> cachedHolograms = Collections.synchronizedSet(new HashSet<>());

    @Suggestions("holograms")
    public List<String> getHologramIds(CommandContext<CommandSender> sender, String input) {
        return cachedHolograms.stream()
                .map(UUID::toString)
                .filter(str -> str.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }

    @CommandMethod("jyrafholo create")
    @CommandPermission("jholo.admin")
    public void create(Player sender) {
        Location location = sender.getLocation();
        UUID uuid = UUID.randomUUID();
        PacketHologram hologram = new PacketHologram(uuid);
        hologram.setGlobal(true);
        hologram.setLocation(location);
        this.service.getRepository(PacketHologram.class, sender.getWorld())
                .flatMap(worldRepository -> worldRepository.add(hologram))
                .subscribe();
        this.cachedHolograms.add(uuid);
        sender.sendMessage("created hologram with id " + uuid);
    }

    @CommandMethod("jyrafholo add <hologram> <line>")
    @CommandPermission("jholo.admin")
    public void addLine(Player sender, @Argument(value = "hologram", suggestions = "holograms") String hologramId, @Argument("line") String line) {
        if (line.isBlank()) return;
        Location location = sender.getLocation();
        this.service.getRepository(PacketHologram.class, sender.getWorld())
                .publishOn(Schedulers.boundedElastic())
                .subscribe(worldRepository -> worldRepository.get()
                        .filter(hologram -> hologram.getUniqueId().equals(UUID.fromString(hologramId)))
                        .subscribe(hologram -> {
                            HologramLine hologramLine = new HologramTextLine(location, EntityType.ARMOR_STAND);
                            hologramLine.setLocation(hologramLine.getLocation());
                            hologram.addLine(hologramLine);
                        }));
        sender.sendMessage("added line to " + hologramId + "!");
    }

}