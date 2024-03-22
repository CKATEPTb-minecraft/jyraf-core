package dev.ckateptb.minecraft.jyraf.packet.factory.V1_19_3;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_17.V1_17PacketFactory;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.UUID;

public class V1_19_3PacketFactory extends V1_17PacketFactory {

    @Override
    public void addTabPlayer(Player player, PacketEntity entity) {
        if (entity.getType() != org.bukkit.entity.EntityType.PLAYER) return;
        UUID uniqueId = entity.getUniqueId();
        int entityId = entity.getId();
        UserProfile profile = new UserProfile(uniqueId, Integer.toString(entityId));
        // TODO APPLY SKIN
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile, false, 1, GameMode.CREATIVE, Text.of("npc-" + entityId), null
        );
        this.sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
        ), info, info));
    }

    @Override
    public void removeTabPlayer(Player player, PacketEntity entity) {
        this.sendPacket(player, new WrapperPlayServerPlayerInfoRemove(entity.getUniqueId()));
    }
}
