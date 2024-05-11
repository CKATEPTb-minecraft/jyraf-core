package dev.ckateptb.minecraft.jyraf.packet.managed;

import dev.ckateptb.minecraft.jyraf.collider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Getter
public abstract class RepositoryManaged {
    private static final Predicate<Player> viewedByEveryone = player -> true;
    protected final Set<Predicate<Player>> viewFilter = Collections.synchronizedSet(new HashSet<>());
    protected final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private final Set<PacketGoal<RepositoryManaged>> goals = Collections.synchronizedSet(ConcurrentHashMap.newKeySet());

    public boolean addFilter(Predicate<Player> filter) {
        return this.viewFilter.add(filter);
    }

    public boolean removeFilter(Predicate<Player> filter) {
        return this.viewFilter.add(filter);
    }

    public boolean canView(Player player) {
        return this.viewFilter.stream().anyMatch(filter -> filter.test(player));
    }

    public boolean isViewed(Player player) {
        return this.currentViewers.contains(player);
    }

    public boolean isViewedByEveryone() {
        return this.viewFilter.contains(viewedByEveryone);
    }

    public void setViewedByEveryone(boolean value) {
        boolean viewedByEveryone = this.isViewedByEveryone();
        if (!value && viewedByEveryone) {
            this.removeFilter(RepositoryManaged.viewedByEveryone);
        }
        if (value && !viewedByEveryone) {
            this.addFilter(RepositoryManaged.viewedByEveryone);
        }
    }

    @SuppressWarnings("unchecked")
    public void addGoal(PacketGoal<? extends RepositoryManaged> goal) {
        this.goals.add((PacketGoal<RepositoryManaged>) goal);
    }

    @SuppressWarnings("unchecked")
    public void removeGoal(PacketGoal<? extends RepositoryManaged> goal) {
        this.goals.remove((PacketGoal<RepositoryManaged>) goal);
    }

    public abstract Location getLocation();

    public void tick() {
        Location location = this.getLocation();
        Colliders.sphere(location, Property.VIEW_DISTANCE.parse(this, Double.class))
                .findEntities()
                .filter(entity -> entity instanceof Player)
                .cast(Player.class)
                .sort((o1, o2) -> {
                    Location first = o1.getLocation();
                    Location second = o2.getLocation();
                    return (int) (first.distanceSquared(location) - second.distanceSquared(location));
                })
                .filter(this::canView)
                .collectList()
                .subscribe(players -> {
                    for (PacketGoal<RepositoryManaged> goal : this.getGoals().stream()
                            .sorted(Comparator.comparing(PacketGoal::getPriority)).toList()) {
                        PacketGoal.Result result = goal.onTick(this, players.toArray(new Player[0]));
                        if (result == PacketGoal.Result.PENDING) break;
                        if (result == PacketGoal.Result.DESTROY) this.removeGoal(goal);
                    }
                });
    }

    public abstract void spawn(Collection<Player> players);

    public abstract void despawn(Collection<Player> players);
}
