package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.EntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.other.ArmorStandMeta;
import dev.ckateptb.minecraft.jyraf.placeholder.PAPI;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PacketHologram extends PacketEntity {
    @Getter
    private final List<PacketEntity> lines = new ArrayList<>();
    private final Map<String, String> placeholders = new ConcurrentHashMap<>();

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
        if (index >= this.lines.size()) {
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

    public synchronized void addPlaceholder(String key, String value) {
        this.placeholders.put(key, value);
    }

    public synchronized String getPlaceholder(String key) {
        return this.placeholders.get(key);
    }

    public synchronized boolean removePlaceholder(String key) {
        return this.placeholders.remove(key) != null;
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
        this.getGoals().forEach(goal -> goal.beforeDespawn(this, players.toArray(new Player[0])));
        Flux.fromIterable(this.lines).subscribe(line -> line.despawn(players));
        this.getGoals().forEach(goal -> goal.onDespawn(this, players.toArray(new Player[0])));
    }

    @Override
    public void metadata(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.metadata(players));
    }

    @Override
    public void refresh(Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> this.applyPlaceholders(players, line, player -> line.metadata(List.of(player))));
        this.teleport(this.location, players);
    }

    private void applyPlaceholders(Collection<Player> players, PacketEntity line, Consumer<Player> consumer) {
        Flux.fromIterable(players)
                .subscribe(player -> {
                    Component original = line.meta.getCustomName();
                    String[] placeholders = this.placeholders.entrySet()
                            .stream()
                            .flatMap(entity -> Stream.of(entity.getKey(), entity.getValue()))
                            .toArray(String[]::new);
                    Component papi = Text.of(PAPI.setPlaceholders(player, Text.of(original)), placeholders);
                    line.meta.setCustomName(papi);
                    consumer.accept(player);
                    line.meta.setCustomName(original);
                });
    }

    @Override
    public void spawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeSpawn(this, players.toArray(new Player[0])));
        Flux.fromIterable(this.lines)
                .subscribe(line -> this.applyPlaceholders(players, line, player -> line.spawn(List.of(player))));
        this.getGoals().forEach(goal -> goal.onSpawn(this, players.toArray(new Player[0])));
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
        this.getGoals().forEach(goal -> goal.onTeleport(this, location.clone(), players.toArray(new Player[0])));
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
        this.getGoals().forEach(goal -> goal.onVelocity(this, vector.clone(), players.toArray(new Player[0])));
    }

    @Override
    public void rotate(float yaw, float pitch, Collection<Player> players) {
        Flux.fromIterable(this.lines).subscribe(line -> line.rotate(yaw, pitch, players));
    }
}
