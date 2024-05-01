package dev.ckateptb.minecraft.jyraf.packet.factory.V1_19_3;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketPlayer;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_17.V1_17PacketFactory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class V1_19_3PacketFactory extends V1_17PacketFactory {

    @Override
    public void addPlayerToTab(@NotNull Player player, @NotNull PacketPlayer entity) {
        if (entity.getType() != org.bukkit.entity.EntityType.PLAYER) return;
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                entity.getProfile(), false, 1, GameMode.CREATIVE, Text.of("npc-" + entity.getId()), null
        );
        this.sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
        ), info, info));
    }

    @Override
    public void removePlayerFromTab(@NotNull Player player, @NotNull PacketPlayer entity) {
        this.sendPacket(player, new WrapperPlayServerPlayerInfoRemove(entity.getUuid()));
    }
}
