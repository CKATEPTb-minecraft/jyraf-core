package dev.ckateptb.minecraft.jyraf.packet.entity.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.patheloper.api.pathing.strategy.strategies.WalkablePathfinderStrategy;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Getter
@Component
@RequiredArgsConstructor
public class NpcCommand implements Command {
    private final WorldService service;

    @CommandMethod("npc <type>")
    @CommandPermission("jnpcs.admin")
    public void npc(Player player, @Argument("type") EntityType type) {
        PacketEntity packetEntity = new PacketEntity(SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), type, player.getLocation());
        packetEntity.setGlobal(true);
        packetEntity.setLookType(LookType.PER_PLAYER);
        packetEntity.setGravity(true);
        packetEntity.setPathfinderStrategy(new WalkablePathfinderStrategy());
        this.follow(packetEntity, player).subscribe();
        this.service.getWorld(player.getWorld().getUID())
                .flatMap(worldRepository -> worldRepository.add(packetEntity))
                .subscribe();
    }

    private Mono<Location> follow(PacketEntity entity, Player player) {
        System.out.println("follow");
        return entity.moveTo(player.getLocation())
                .flatMap(location -> this.follow(entity, player));
    }
}
