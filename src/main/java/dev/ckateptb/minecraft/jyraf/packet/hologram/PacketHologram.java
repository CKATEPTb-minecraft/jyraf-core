package dev.ckateptb.minecraft.jyraf.packet.hologram;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.hologram.line.HologramLine;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PacketHologram {

    @Getter
    protected final UUID uniqueId;
    private final Set<Player> allowedViewers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    private final Set<HologramLine> lines = Collections.synchronizedSet(new HashSet<>());
    @Getter
    @Setter
    private boolean global = true;
    @Getter
    @Setter
    protected Location location;
    // todo
    @Getter
    @Setter
    private Object interactHandler = null;

    public PacketHologram() {
        this.uniqueId = UUID.randomUUID();
    }

    public PacketHologram(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void tick() {
        Location location = this.getLocation();
        Colliders.sphere(location, 20)
                .affectEntities(entities -> {
                    Flux<Player> flux = entities
                            .filter(entity -> entity instanceof Player)
                            .cast(Player.class)
                            .sort((o1, o2) -> {
                                Location first = o1.getLocation();
                                Location second = o2.getLocation();
                                return (int) (first.distanceSquared(location) - second.distanceSquared(location));
                            });
                    if (!this.global) flux = flux.filter(this.allowedViewers::contains);
                    flux.collectList()
                            .doOnNext(players -> {
                                this.currentViewers.removeIf(player -> {
                                    if (players.contains(player) && player.isOnline()) return false;
                                    this.hide(player);
                                    return true;
                                });
                                players.forEach(player -> {
                                    if (!this.currentViewers.add(player)) return;
                                    this.show(player);
                                });
                            })
                            .subscribe();
                });
    }

    public boolean removeLine(HologramLine line) {
        line.remove();
        return this.lines.remove(line);
    }

    public boolean addLine(HologramLine line) {
        this.currentViewers.stream().filter(line::isDisplayed)
                .forEach(line::show);
        return this.lines.add(line);
    }

    public boolean show(Player player) {
        boolean added = this.allowedViewers.add(player);
        if (added) {
            this.lines.forEach(line -> line.show(player));
        }
        return added;
    }

    public boolean hide(Player player) {
        boolean contained = this.allowedViewers.remove(player);
        if (contained) {
            this.lines.forEach(line -> line.hide(player));
        }
        return contained;
    }

    public boolean canView(Player player) {
        return this.global || this.allowedViewers.contains(player);
    }

    public boolean isViewed(Player player) {
        return this.currentViewers.contains(player);
    }

    public void remove() {
        this.currentViewers.forEach(this::hide);
    }

    public World getWorld() {
        return this.location.getWorld();
    }

}