package dev.ckateptb.minecraft.jyraf.packet.block.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

@Getter
@Component
@RequiredArgsConstructor
public class BlockCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafblock <type>")
    @CommandPermission("jblocks.admin")
    public void block(Player commandSender, @Argument("type") Material type) {
        if (!type.isBlock()) return;
        RayTraceResult result = commandSender.rayTraceBlocks(5.0);
        if (result == null) return;
        if (result.getHitBlock() == null) return;
        if (result.getHitBlock().getType() == Material.AIR) return;
        Location location = result.getHitBlock().getLocation();
        PacketBlock packetBlock = new PacketBlock(location, type.createBlockData());
        packetBlock.setInteractHandler((player, clickType) -> player.sendMessage(clickType.name()));
        this.service.getRepository(PacketBlock.class, commandSender.getWorld())
            .flatMap(worldRepository -> worldRepository.add(packetBlock))
            .subscribe();
    }
}
