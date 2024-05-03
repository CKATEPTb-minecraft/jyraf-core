package dev.ckateptb.minecraft.jyraf.packet.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.PlayerMeta;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.patheloper.api.pathing.strategy.strategies.JumpablePathfinderStrategy;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class PacketPlayer extends PacketLivingEntity {
    private final UserProfile profile;

    protected PacketPlayer(int id, UUID uuid, Location location) {
        super(id, uuid, EntityType.PLAYER, (PlayerMeta) EntityMeta.createMeta(id, EntityTypes.PLAYER), location);
        this.profile = new UserProfile(uuid, Integer.toString(id));
    }

    @Override
    public void spawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeSpawn(this, players.toArray(new Player[0])));
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.addPlayerToTab(player, this);
                factory.spawnPlayer(player, this);
                Location location = this.getLocation();
                factory.rotateEntity(player, this, location.getYaw(), location.getPitch());
                factory.metadataEntity(player, this);
                factory.createEntityTeam(player, this, Property.ENTITY_TEAM.parse(this, TeamColor.class));
                Bukkit.getScheduler().runTaskLaterAsynchronously(Jyraf.getPlugin(), () ->
                        factory.removePlayerFromTab(player, this), 60);
            }
        });
        this.getGoals().forEach(goal -> goal.onSpawn(this, players.toArray(new Player[0])));
    }

    @Override
    public Mono<Boolean> moveTo(Location location) {
        return this.moveTo(location, new JumpablePathfinderStrategy());
    }

    public List<TextureProperty> getSkin() {
        return this.profile.getTextureProperties();
    }

    public void setSkin(List<TextureProperty> skin) {
        this.profile.setTextureProperties(skin);
    }

    @Override
    public PlayerMeta getMeta() {
        return (PlayerMeta) super.getMeta();
    }
}
