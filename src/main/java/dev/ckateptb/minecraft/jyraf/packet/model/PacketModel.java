package dev.ckateptb.minecraft.jyraf.packet.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class PacketModel {
    private final Set<Player> audience = Collections.synchronizedSet(ConcurrentHashMap.newKeySet());
    private final Set<Predicate<Player>> filters = Collections.synchronizedSet(ConcurrentHashMap.newKeySet());
    @Getter
    protected Location location;
    @Getter
    @Setter
    private double viewDistance = Bukkit.getViewDistance() * 2;

    public void show(Player... players) {
        if (this.location == null) return;
        for (Player player : players) {
            if (this.audience.add(player)) {
                this.onShow(player);
            }
        }
    }

    public void hide(Player... players) {
        if (this.location == null) return;
        for (Player player : players) {
            if (this.audience.remove(player)) {
                this.onHide(player);
            }
        }
    }

    public boolean addFilter(Predicate<Player> filter) {
        return this.filters.add(filter);
    }

    public boolean removeFilter(Predicate<Player> filter) {
        return this.filters.remove(filter);
    }

    public boolean canView(Player player) {
        if (this.location == null) return false;
        if (player.getLocation().distance(this.location) > this.viewDistance) return false;
        if (this.filters.isEmpty()) return true;
        return this.filters.stream().allMatch(predicate -> predicate.test(player));
    }

    public Collection<Player> getAudience() {
        return Collections.unmodifiableCollection(this.audience);
    }

    protected abstract void onShow(Player player);

    protected abstract void onHide(Player player);
}
