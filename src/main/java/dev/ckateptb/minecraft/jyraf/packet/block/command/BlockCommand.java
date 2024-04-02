package dev.ckateptb.minecraft.jyraf.packet.block.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.BlockAction;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

@Getter
@Component
@RequiredArgsConstructor
public class BlockCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafblock <material>")
    @CommandPermission("jblocks.admin")
    public void block(Player sender, @Argument("material") Material material) {
        RayTraceResult result = sender.rayTraceBlocks(3.0);
        if (result == null) return;
        Block block = result.getHitBlock();
        if (block == null) return;
        Location location = block.getLocation();
        PacketBlock packetBlock = new PacketBlock(location, material.createBlockData());
        packetBlock.setInteractionHandler((player, button) -> {
            player.sendMessage(button.name());
            BlockAction action = button == MouseButton.RIGHT ? BlockAction.OPEN : BlockAction.CLOSE;
            if (material == Material.CHEST || material == Material.ENDER_CHEST ||
                    material == Material.TRAPPED_CHEST || material == Material.SHULKER_BOX) {
                packetBlock.playAction(action);
            }
        });
        this.service.getRepository(PacketBlock.class, sender.getWorld())
                .flatMap(worldRepository -> worldRepository.add(packetBlock))
                .subscribe();
    }
}
