package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.other.ArmorStandMeta;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketHologram extends PacketEntity {
    private final List<PacketEntity> lines = new ArrayList<>();

    protected PacketHologram(int id, UUID uuid, Location location) {
        super(id, uuid, EntityType.ARMOR_STAND,
                EntityMeta.createMeta(id, SpigotConversionUtil.fromBukkitEntityType(EntityType.ARMOR_STAND)), location);
    }

    public @Nullable Component getLine(int index) {
        if (index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index).meta.getCustomName();
    }

    public void setLine(int index, Component line) {
        PacketEntity entity = PacketEntity.entity(this.type, this.location);
        if (entity.meta instanceof ArmorStandMeta armorStandMeta) {
            armorStandMeta.setCustomName(line);
            armorStandMeta.setCustomNameVisible(true);
            armorStandMeta.setInvisible(true);
            armorStandMeta.setHasNoGravity(true);
            armorStandMeta.setSmall(true);
            armorStandMeta.setMarker(true);
        }
        if(index >= this.lines.size()) {
            this.lines.add(entity);
        } else {
            this.lines.get(index).despawn(this.currentViewers);
            this.lines.set(index, entity);
        }
        this.teleport(this.location, this.currentViewers);
        entity.spawn(this.currentViewers);
    }

    public void addLine(@Nullable Component line) {
        this.setLine(lines.size(), line);
    }

    @Override
    public Mono<Boolean> moveTo(Location location, PathfinderStrategy strategy) {
        // Not supported
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> moveTo(Location location) {
        // Not supported
        return Mono.just(false);
    }

    @Override
    public void despawn(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.despawn(players));
    }

    @Override
    public void metadata(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.metadata(players));
    }

    @Override
    public void refresh(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.refresh(players));
        this.teleport(this.location, players);
    }

    @Override
    public void spawn(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.spawn(players));
    }

    @Override
    public void teleport(Location location, Collection<Player> players) {
        Validate.isTrue(this.location.getWorld().equals(location.getWorld()), "World does not match");
        this.location.set(location.getX(), location.getY(), location.getZ());
        this.location.setYaw(location.getYaw());
        this.location.setPitch(location.getPitch());
        Flux.fromIterable(this.lines)
                .index()
                .subscribe(objects -> {
                    Long index = objects.getT1();
                    PacketEntity line = objects.getT2();
                    line.teleport(location.clone().add(0, -0.3 * index, 0), players);
                });
    }

    @Override
    public void velocity(Vector vector, Collection<Player> players) {
        this.location.add(vector);
        Flux.fromIterable(this.lines)
                .index()
                .subscribe(objects -> {
                    Long index = objects.getT1();
                    PacketEntity line = objects.getT2();
                    line.velocity(vector.clone().add(new Vector(0, -0.3 * index, 0)), players);
                });
    }

    @Override
    public void rotate(float yaw, float pitch, Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.rotate(yaw, pitch, players));
    }
}
