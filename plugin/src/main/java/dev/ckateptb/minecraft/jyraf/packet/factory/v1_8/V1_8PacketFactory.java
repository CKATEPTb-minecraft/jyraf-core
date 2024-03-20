package dev.ckateptb.minecraft.jyraf.packet.factory.v1_8;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.packetevents.api.manager.player.PlayerManager;
import dev.ckateptb.minecraft.packetevents.api.protocol.entity.type.EntityType;
import dev.ckateptb.minecraft.packetevents.api.protocol.player.ClientVersion;
import dev.ckateptb.minecraft.packetevents.api.protocol.player.GameMode;
import dev.ckateptb.minecraft.packetevents.api.protocol.player.UserProfile;
import dev.ckateptb.minecraft.packetevents.api.protocol.world.Location;
import dev.ckateptb.minecraft.packetevents.api.util.Vector3d;
import dev.ckateptb.minecraft.packetevents.api.wrapper.PacketWrapper;
import dev.ckateptb.minecraft.packetevents.api.wrapper.play.server.*;
import dev.ckateptb.minecraft.packetevents.impl.util.SpigotConversionUtil;
import dev.ckateptb.minecraft.packetevents.internal.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class V1_8PacketFactory {
    private final static CachedReference<PlayerManager> PACKET_MANAGER = new CachedReference<>(() ->
            Jyraf.getPlugin().getPacketApi().getPlayerManager());
    private final static CachedReference<ClientVersion> CLIENT_VERSION = new CachedReference<>(() ->
            Jyraf.getPlugin().getPacketApi().getServerManager().getVersion().toClientVersion());

    public void sendMetadata(Player player, PacketEntity entity) {
        // TODO implement
    }

    public void teleport(Player player, PacketEntity entity, boolean onGround) {
        int entityId = entity.getId();
        org.bukkit.Location location = entity.getLocation();
        this.sendPacket(player, new WrapperPlayServerEntityTeleport(entityId,
                SpigotConversionUtil.fromBukkitLocation(location), onGround));
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(entityId, location.getYaw()));
    }

    public void rotate(Player player, PacketEntity entity, float yaw, float pitch) {
        int entityId = entity.getId();
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(entityId, yaw));
        this.sendPacket(player, new WrapperPlayServerEntityRotation(entityId, yaw, pitch, true));
    }

    public void createTeam(Player player, PacketEntity entity) {
        String team = "npc-team-" + entity.getId();
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.REMOVE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null));
        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.text(" "), null, null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                Optional.ofNullable(entity.getTeamColor()).orElse(TeamColor.WHITE).getKyori(),
                WrapperPlayServerTeams.OptionData.NONE
        );
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.CREATE, info));
        String id = entity.getType() == org.bukkit.entity.EntityType.PLAYER ? Integer.toString(entity.getId()) : entity.getUniqueId().toString();
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, id));
    }

    public void addTabPlayer(Player player, PacketEntity entity) {
        if (entity.getType() != org.bukkit.entity.EntityType.PLAYER) return;
        UUID uniqueId = entity.getUniqueId();
        int entityId = entity.getId();
        UserProfile profile = new UserProfile(uniqueId, Integer.toString(entityId));
        // TODO APPLY SKIN
//        profile.setTextureProperties();
        this.sendPacket(player, new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData(
                Component.text("NPC" + entityId),
                profile, GameMode.CREATIVE, 1)));
    }

    public void removeTabPlayer(Player player, PacketEntity entity) {
        if (entity.getType() != org.bukkit.entity.EntityType.PLAYER) return;
        this.sendPacket(player, new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData(null,
                new UserProfile(entity.getUniqueId(), null), null, -1)));
    }

    public void spawnPlayer(Player player, PacketEntity entity) {
        Mono.defer(() -> {
                    this.addTabPlayer(player, entity);
                    return Mono.just(true);
                })
                .doFinally((signalType) -> Bukkit.getScheduler().runTaskLaterAsynchronously(Jyraf.getPlugin(),
                        () -> this.removeTabPlayer(player, entity), 60))
                .subscribe(ignored -> {
                    this.createTeam(player, entity);
                    org.bukkit.Location location = entity.getLocation();
                    int entityId = entity.getId();
                    float yaw = location.getYaw();
                    this.sendPacket(player, new WrapperPlayServerSpawnPlayer(entityId, entity.getUniqueId(),
                            SpigotConversionUtil.fromBukkitLocation(location).getPosition(),
                            yaw, location.getPitch(), Collections.emptyList()));
                    this.sendPacket(player, new WrapperPlayServerEntityHeadLook(entityId, yaw));
                    this.sendMetadata(player, entity);
                    this.createTeam(player, entity);
                });
    }

    public void spawnEntity(Player player, PacketEntity entity) {
        EntityType type = SpigotConversionUtil.fromBukkitEntityType(entity.getType());
        boolean legacy = type.getLegacyId(CLIENT_VERSION.force()) == -1;
        Location location = SpigotConversionUtil.fromBukkitLocation(entity.getLocation());
        this.sendPacket(player, legacy ?
                new WrapperPlayServerSpawnLivingEntity(entity.getId(), entity.getUniqueId(), type, location.getPosition(),
                        location.getYaw(), location.getPitch(), location.getYaw(), new Vector3d(), Collections.emptyList()) :
                new WrapperPlayServerSpawnEntity(entity.getId(), Optional.of(entity.getUniqueId()), type, location.getPosition(),
                        location.getPitch(), location.getYaw(), location.getYaw(), 0, Optional.empty()));
        this.sendMetadata(player, entity);
        this.createTeam(player, entity);
    }

    public void despawnEntity(Player player, PacketEntity entity) {
        this.sendPacket(player, new WrapperPlayServerDestroyEntities(entity.getId()));
    }

    public void sendPacket(Player player, PacketWrapper<?> packet) {
        PACKET_MANAGER.get().ifPresent(playerManager -> playerManager.sendPacket(player, packet));
    }
}
