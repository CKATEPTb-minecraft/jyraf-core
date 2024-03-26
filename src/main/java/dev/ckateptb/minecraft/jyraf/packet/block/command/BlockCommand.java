package dev.ckateptb.minecraft.jyraf.packet.block.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Getter
@Component
@RequiredArgsConstructor
public class BlockCommand implements Command {
    private final WorldRepositoryService service;
    private final Stream<String> allowedMaterialNames = Arrays.stream(Material.values())
        .filter(not(Material::isItem))
        .map(Material::name);

    @Suggestions("allowedMaterials")
    public List<String> getAllowedMaterials(CommandContext<CommandSender> sender, String input) {
        if (!(sender.getSender() instanceof Player)) return new ArrayList<>();
        return allowedMaterialNames
            .filter(material -> material.toLowerCase().startsWith(input.toLowerCase()))
            .toList();
    }

    @CommandMethod(value = "jyrafblock <material>", requiredSender = Player.class)
    @CommandPermission("jblocks.admin")
    public void block(Player sender, @Argument(value = "material", suggestions = "allowedMaterials") Material material) {
        if (material.isItem()) return;
        RayTraceResult result = sender.rayTraceBlocks(5.0);
        if (result == null) return;
        if (result.getHitBlock() == null) return;
        if (result.getHitBlock().getType() == Material.AIR) return;
        Location location = result.getHitBlock().getLocation();
        PacketBlock packetBlock = new PacketBlock(location, material.createBlockData());
        packetBlock.setInteractHandler((player, clickType) -> player.sendMessage(clickType.name()));
        this.service.getRepository(PacketBlock.class, sender.getWorld())
            .flatMap(worldRepository -> worldRepository.add(packetBlock))
            .subscribe();
    }
}
