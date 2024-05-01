package dev.ckateptb.minecraft.jyraf.packet.entity.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketPlayer;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.goal.FallEntityGoal;
import dev.ckateptb.minecraft.jyraf.packet.entity.goal.LookEntityGoal;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.display.BlockDisplayMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.other.FallingBlockMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.PlayerMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Component
@RequiredArgsConstructor
public class NpcCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafnpc <type>")
    @CommandPermission("jnpcs.admin")
    public void npc(Player sender, @Argument("type") EntityType type) {
        PacketEntity entity = PacketEntity.entity(type, sender.getLocation());
        Property.ENTITY_TEAM.apply(entity, TeamColor.GOLD);
        entity.addGoal(new FallEntityGoal(PacketGoal.Priority.NORMAL));
        entity.addGoal(new LookEntityGoal(PacketGoal.Priority.NORMAL, LookEntityGoal.Mode.PER_PLAYER));
        EntityMeta meta = entity.getMeta();
        if (entity instanceof PacketPlayer player) {
            List<TextureProperty> textureProperties = Skin.from(sender).block();
            player.setSkin(textureProperties);
        }
        if (meta instanceof FallingBlockMeta fallingBlockMeta) {
            fallingBlockMeta.setBlockStateId(SpigotConversionUtil.fromBukkitBlockData(Material.STONE.createBlockData()).getGlobalId());
            fallingBlockMeta.setHasNoGravity(true);
        }
        if (meta instanceof BlockDisplayMeta blockDisplayMeta) {
            blockDisplayMeta.setBlockId(SpigotConversionUtil.fromBukkitBlockData(Material.SAND.createBlockData()).getGlobalId());
        }
        if (meta instanceof PlayerMeta playerMeta) {
            playerMeta.setCapeEnabled(true);
            playerMeta.setHatEnabled(true);
            playerMeta.setJacketEnabled(true);
            playerMeta.setLeftLegEnabled(true);
            playerMeta.setRightLegEnabled(true);
            playerMeta.setLeftSleeveEnabled(true);
            playerMeta.setRightSleeveEnabled(true);
        }
        meta.setGlowing(true);
        entity.setViewedByEveryone(true);
        this.service.getRepository(PacketEntity.class, sender.getWorld())
                .flatMap(repository -> repository.add(entity))
                .subscribe();
    }
}
