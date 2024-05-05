package dev.ckateptb.minecraft.jyraf.packet.block.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBellBlock;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketChestBlock;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;

// TODO Remove. We don't need debugging commands in production
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
        BlockData blockData = material.createBlockData();
        if (blockData instanceof Directional directional) {
            directional.setFacing(sender.getFacing().getOppositeFace());
        }
        PacketBlock packetBlock = PacketBlock.of(blockData, location);
        packetBlock.setViewedByEveryone(true);
        if (packetBlock instanceof PacketChestBlock chestBlock) {
            chestBlock.addGoal(new PacketGoal<PacketChestBlock>(PacketGoal.Priority.NORMAL) {
                @Override
                public void onSpawn(PacketChestBlock entry, Player... players) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Jyraf.getPlugin(), () ->
                                    entry.open(Arrays.asList(players))
                            , 60);
                }
            });
        }
        if (packetBlock instanceof PacketBellBlock bellBlock) {
            bellBlock.addGoal(new PacketGoal<PacketBellBlock>(PacketGoal.Priority.NORMAL) {
                @Override
                public void onSpawn(PacketBellBlock entry, Player... players) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Jyraf.getPlugin(), () ->
                                    entry.ring(BlockFace.NORTH, Arrays.asList(players))
                            , 60);
                }
            });
        }
        this.service.getRepository(PacketBlock.class, sender.getWorld())
                .publishOn(Schedulers.boundedElastic())
                .flatMap(worldRepository -> worldRepository.add(packetBlock))
                .subscribe();
    }
}
