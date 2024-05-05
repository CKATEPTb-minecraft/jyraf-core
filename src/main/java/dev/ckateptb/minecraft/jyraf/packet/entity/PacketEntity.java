package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.goal.MoveEntityGoal;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.LivingEntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.goal.view.ViewGoal;
import dev.ckateptb.minecraft.jyraf.packet.managed.RepositoryManaged;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.pathing.strategy.strategies.DirectPathfinderStrategy;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

// BlockDisplay doesn't work in packetevents use ItemDisplay instead
@Getter
public class PacketEntity extends RepositoryManaged {
    private final int id;
    private final UUID uuid;
    private final EntityType type;
    private final EntityMeta meta;
    private final Location location;

    protected PacketEntity(int id, UUID uuid, EntityType type, EntityMeta meta, Location location) {
        meta.getMetadata().getEntity().defer(() -> this);
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.meta = meta;
        this.location = location.clone();
        this.addGoal(new ViewGoal());
    }

    public static PacketEntity entity(EntityType type, Location location) {
        if (type.isAlive()) return living(type, location);
        int id = SpigotReflectionUtil.generateEntityId();
        EntityMeta meta = EntityMeta.createMeta(id, SpigotConversionUtil.fromBukkitEntityType(type));
        return new PacketEntity(id, UUID.randomUUID(), type, meta, location);
    }

    public static PacketLivingEntity living(EntityType type, Location location) {
        if (type == EntityType.PLAYER) return player(location);
        Validate.isTrue(type.isAlive(), "entity type is not alive");
        int id = SpigotReflectionUtil.generateEntityId();
        EntityMeta meta = EntityMeta.createMeta(id, SpigotConversionUtil.fromBukkitEntityType(type));
        return new PacketLivingEntity(id, UUID.randomUUID(), type, (LivingEntityMeta) meta, location);
    }

    public static PacketPlayer player(Location location) {
        return new PacketPlayer(SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), location);
    }

    public void rotate(float yaw, float pitch, Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.rotateEntity(player, this, yaw, pitch);
            }
        });
    }

    public void velocity(Vector vector, Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.velocityEntity(player, this, vector);
                this.location.add(vector);
            }
        });
    }

    public void teleport(Location location, Collection<Player> players) {
        Validate.isTrue(this.location.getWorld().equals(location.getWorld()), "World does not match");
        this.location.set(location.getX(), location.getY(), location.getZ());
        this.location.setYaw(location.getYaw());
        this.location.setPitch(location.getPitch());
        PacketFactory.INSTANCE.consume(factory -> {
            boolean onGround = this.isOnGround();
            for (Player player : players) {
                factory.teleportEntity(player, this, onGround);
            }
        });
    }

    public void spawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeSpawn(this, players.toArray(new Player[0])));
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.spawnEntity(player, this);
                factory.metadataEntity(player, this);
                factory.createEntityTeam(player, this, Property.ENTITY_TEAM.parse(this, TeamColor.class));
            }
        });
        this.getGoals().forEach(goal -> goal.onSpawn(this, players.toArray(new Player[0])));
    }

    public void refresh(Collection<Player> players) {
        this.metadata(players);
    }

    public void metadata(Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.metadataEntity(player, this);
            }
        });
    }

    public void despawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeDespawn(this, players.toArray(new Player[0])));
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.despawnEntity(player, this);
            }
        });
        this.getGoals().forEach(goal -> goal.onDespawn(this, players.toArray(new Player[0])));
    }

    public Mono<Boolean> moveTo(Location location) {
        return this.moveTo(location, new DirectPathfinderStrategy());
    }

    public Mono<Boolean> moveTo(Location location, PathfinderStrategy strategy) {
        MoveEntityGoal move = new MoveEntityGoal(location, strategy);
        this.addGoal(move);
        return move.getCompleted();
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public boolean isOnGround() {
        return ImmutableVector.of(this.location)
                .getDistanceAboveGround(this.location.getWorld(), true) < 0.1;
    }
}
