package dev.ckateptb.minecraft.jyraf.packet.block.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.colider.geometry.RayTraceCollider;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@Component
@RequiredArgsConstructor
public class BlockCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafblock <material>")
    @CommandPermission("jblocks.admin")
    public void block(Player sender, @Argument("material") Material material) {
        if (material.isItem()) return;
        RayTraceCollider collider = Colliders.ray(sender, 3.0, 0.01);
        collider.getBlock(true, true, (block) -> true)
            .subscribe(block -> {
                Location location = block.getLocation();
                PacketBlock packetBlock = new PacketBlock(location, material.createBlockData());
                packetBlock.setInteractHandler((player, clickType) -> player.sendMessage(clickType.name()));
                this.service.getRepository(PacketBlock.class, sender.getWorld())
                    .flatMap(worldRepository -> worldRepository.add(packetBlock))
                    .subscribe();
            });
    }
}
