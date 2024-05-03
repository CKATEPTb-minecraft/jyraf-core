package dev.ckateptb.minecraft.jyraf.packet.entity.goal;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.wrapper.PathPosition;
import org.patheloper.mapping.PatheticMapper;
import org.patheloper.mapping.bukkit.BukkitMapper;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MoveEntityGoal extends PacketGoal<PacketEntity> {
    private final Location destiny;
    private final PathfinderStrategy strategy;
    private final CompletableFuture<Boolean> complete = new CompletableFuture<>();
    private final LazyLoader.Later<Iterator<PathPosition>> iterator = LazyLoader.later();
    private Location currentDestiny;
    private Double speed = 0.2;

    public MoveEntityGoal(Location destiny, PathfinderStrategy strategy) {
        this(Priority.LOW, destiny, strategy);

    }

    public MoveEntityGoal(Priority priority, Location destiny, PathfinderStrategy strategy) {
        super(priority);
        this.destiny = destiny;
        this.strategy = strategy;
    }

    @Override
    public Result onTick(PacketEntity entry, Player... players) {
        Location location = entry.getLocation();
        World world = location.getWorld();
        if (this.destiny == null || !this.destiny.getWorld().equals(world)) {
            this.complete.complete(false);
            return Result.DESTROY;
        }
        if (!this.iterator.isDefined()) {
            this.iterator.defer(() -> {
                this.speed = Property.ENTITY_SPEED.parse(entry, Double.class);
                PathPosition from = BukkitMapper.toPathPosition(location);
                PathPosition to = BukkitMapper.toPathPosition(this.destiny);
                return PatheticMapper.newPathfinder().findPath(from, to, this.strategy)
                        .toCompletableFuture().join().getPath().iterator();
            });
        }
        if (this.currentDestiny == null || location.distance(this.currentDestiny) < 0.2) {
            Iterator<PathPosition> iterator = this.iterator.get();
            if (!iterator.hasNext()) {
                this.currentDestiny = null;
                this.complete.complete(true);
                return Result.DESTROY;
            }
            do {
                this.currentDestiny = BukkitMapper.toLocation(iterator.next());
            } while (location.distance(this.currentDestiny) < 1 && iterator.hasNext());
        }
        ImmutableVector origin = ImmutableVector.of(location);
        ImmutableVector destiny = ImmutableVector.of(this.currentDestiny);
        ImmutableVector direction = destiny.subtract(origin).normalize();
        Location next = origin.add(direction.multiply(this.speed))
                .toLocation(world)
                .setDirection(entry.getType() == EntityType.ENDER_DRAGON ? direction.negative() : direction);
        entry.teleport(next, List.of(players));
        return Result.PENDING;
    }

    public Mono<Boolean> getCompleted() {
        return Mono.fromFuture(this.complete);
    }
}
