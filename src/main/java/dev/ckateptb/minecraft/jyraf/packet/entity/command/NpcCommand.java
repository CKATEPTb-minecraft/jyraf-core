package dev.ckateptb.minecraft.jyraf.packet.entity.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketPlayer;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.goal.FallEntityGoal;
import dev.ckateptb.minecraft.jyraf.packet.entity.goal.LookEntityGoal;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.display.BlockDisplayMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.display.ItemDisplayMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.other.FallingBlockMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.PlayerMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.List;

// TODO Remove. We don't need debugging commands in production
@Getter
@Component
@RequiredArgsConstructor
public class NpcCommand implements Command {
    private final WorldRepositoryService service;

    @CommandMethod("jyrafnpc <type> [data]")
    @CommandPermission("jnpcs.admin")
    public void npc(Player sender, @Argument("type") EntityType type, @Argument("data") Material data) {
        PacketEntity entity = PacketEntity.entity(type, sender.getLocation());
        Property.ENTITY_TEAM.apply(entity, TeamColor.GOLD);
        entity.addGoal(new FallEntityGoal());
        entity.addGoal(new LookEntityGoal(LookEntityGoal.Mode.PER_PLAYER));
        EntityMeta meta = entity.getMeta();
        if (entity instanceof PacketPlayer player) {
            List<TextureProperty> textureProperties = Skin.from(sender).block();
            player.setSkin(textureProperties);
        }
        if (meta instanceof FallingBlockMeta fallingBlockMeta) {
            fallingBlockMeta.setBlockStateId(SpigotConversionUtil.fromBukkitBlockData(data.createBlockData()).getGlobalId());
            fallingBlockMeta.setHasNoGravity(true);
        }
        if (meta instanceof BlockDisplayMeta blockDisplayMeta) {
            blockDisplayMeta.setBlockId(SpigotConversionUtil.fromBukkitBlockData(data.createBlockData()).getGlobalId());
            blockDisplayMeta.setTranslation(new Vector3f());
            Quaternionf quaternionf = new Quaternionf(new AxisAngle4f());
            Quaternion4f quaternion4f = new Quaternion4f(quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w);
            blockDisplayMeta.setLeftRotation(quaternion4f);
            blockDisplayMeta.setScale(new Vector3f());
            blockDisplayMeta.setRightRotation(quaternion4f);
        }
        if(meta instanceof ItemDisplayMeta itemDisplayMeta) {
            itemDisplayMeta.setItem(SpigotConversionUtil.fromBukkitItemStack(Menu.builder().item(data).build()));
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
        if (entity instanceof PacketPlayer) {
            follow(sender, entity);
        }
    }

    private void follow(Player sender, PacketEntity entity) {
        entity.moveTo(sender.getLocation()).doFinally(signalType -> this.follow(sender, entity)).subscribe();
    }
}
