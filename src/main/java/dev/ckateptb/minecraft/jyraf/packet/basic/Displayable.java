package dev.ckateptb.minecraft.jyraf.packet.basic;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.PacketEntry;
import dev.ckateptb.minecraft.jyraf.packet.basic.displayable.IDisplayable;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import dev.ckateptb.minecraft.jyraf.packet.trait.implementation.DisplayableTrait;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

public abstract class Displayable<T extends Displayable<T>> extends PacketEntry implements IDisplayable {

    protected final Set<Player> allowedViewers = Collections.synchronizedSet(new HashSet<>());
    protected final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    @Getter
    @Setter
    protected boolean global;
    @Getter
    @NotNull
    protected Location location;

    public Displayable(@NotNull Location location) {
        this(location, true);
    }

    public Displayable(@NotNull Location location, boolean global) {
        this(location, List.of());
        this.global = global;
    }

    public Displayable(@NotNull Location location, @NotNull Collection<Player> allowedViewers) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(allowedViewers);
        this.location = location;
        this.global = false;
        this.allowedViewers.addAll(allowedViewers);
        addTrait(new DisplayableTrait<>(Displayable.class));
    }

    @Override
    @SuppressWarnings("unchecked")
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
                    Mono<List<Player>> mono = flux.collectList();
                    for (PacketTrait<?> unknownTrait : this.getTraits()) {
                        if (!unknownTrait.getEntryClass().isAssignableFrom(getClass())) continue;
                        PacketTrait<T> trait = (PacketTrait<T>) unknownTrait;
                        if (trait.isCancelled()) continue;
                        trait.tick(mono, (T) this);
                        mono.doOnNext(players ->
                                players.forEach(player ->
                                        trait.tick(player, (T) this))).subscribe();
                    }
                });
    }

    @Override
    public @NotNull Flux<Player> getAllowedViewers() {
        return Flux.fromIterable(this.allowedViewers);
    }

    @Override
    public @NotNull Flux<Player> getCurrentViewers() {
        return Flux.fromIterable(this.currentViewers);
    }

    public @NotNull Set<Player> getOriginalCurrentViewers() {
        return this.currentViewers;
    }

    @Override
    public abstract void destroy(Player player);

    @Override
    public abstract void display(Player player);

    @Override
    public boolean show(Player player) {
        return this.allowedViewers.add(player);
    }

    @Override
    public boolean hide(Player player) {
        return this.allowedViewers.remove(player);
    }

    @Override
    public boolean canView(Player player) {
        return this.global || this.allowedViewers.contains(player);
    }

    @Override
    public boolean isViewed(Player player) {
        return this.currentViewers.contains(player);
    }

}