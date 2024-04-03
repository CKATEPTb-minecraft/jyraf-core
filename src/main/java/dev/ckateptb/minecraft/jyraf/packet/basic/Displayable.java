package dev.ckateptb.minecraft.jyraf.packet.basic;

import dev.ckateptb.minecraft.jyraf.packet.PacketEntry;
import dev.ckateptb.minecraft.jyraf.packet.basic.displayable.IDisplayable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

import java.util.*;

public abstract class Displayable extends PacketEntry implements IDisplayable {

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