package dev.ckateptb.minecraft.jyraf.packet.entity.player;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.UUID;

public class PacketPlayer extends PacketEntity {
    public PacketPlayer(int id, UUID uniqueId, Location location) {
        super(id, uniqueId, EntityType.PLAYER, location);
    }

    @Override
    protected void spawn(Player player) {
        String name = "npc-" + this.id;
        UserProfile profile = new UserProfile(this.uniqueId, name);
        Skin.from(player)
                .doOnNext(profile::setTextureProperties)
                .doFinally(signalType -> Bukkit.getScheduler()
                        .runTaskLaterAsynchronously(Jyraf.getPlugin(), () ->
                                this.sendPacket(player, new WrapperPlayServerPlayerInfoRemove(this.uniqueId)), 60)
                )
                .subscribe(textureProperty -> {
                    WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                            profile, false, 1, GameMode.CREATIVE, Text.of(name), null
                    );
                    this.sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(
                            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                    ), info, info));
                    this.setTeam(player, TeamColor.WHITE);
                    super.spawn(player);
                    this.lookAt(player, this.location.getYaw(), this.location.getPitch());
                    // TODO Send Metadata
                });
    }
}
