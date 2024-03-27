package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.enums.ClickType;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.patheloper.api.pathing.result.PathfinderResult;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.pathing.strategy.strategies.DirectPathfinderStrategy;
import org.patheloper.api.wrapper.PathPosition;
import org.patheloper.mapping.PatheticMapper;
import org.patheloper.mapping.bukkit.BukkitMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

// TODO Implement properties like a
//  skin, glow, entity type data (villager type etc.)
//  Holograms, Multiple NameTags (based on Holograms)
//  Equipments, Poses, States
//  Dropped Item, Item Display, Block Display, Text Display
//  Implement 1.16.5 support
public class PacketEntity {
    @Getter
    protected final int id;
    @Getter
    protected final UUID uniqueId;
    @Getter
    protected final EntityType type;
    private final Set<Player> allowedViewers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    @Getter
    @Setter
    private boolean global = true; // means that any player can see the entity
    protected Location location;
    @Getter
    @Setter
    private LookType lookType = LookType.FIXED;
    @Getter
    @Setter
    private boolean gravity = false;
    @Getter
    @Setter
    private double speed = 0.2;
    @Getter
    @Setter
    private PathfinderStrategy pathfinderStrategy = new DirectPathfinderStrategy();
    private Tuple2<Iterator<PathPosition>, CompletableFuture<Location>> destiny = null;
    private Location currentPath;
    @Getter
    @Setter
    private TeamColor teamColor = TeamColor.WHITE;
    @Getter
    @Setter
    private PacketEntityInteractHandler interactHandler = (player, clickType) -> {
    };

    public PacketEntity(int id, UUID uniqueId, EntityType type, Location location) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.type = type;
        this.location = location;
    }

    public void tick() {
        Colliders.sphere(this.location, 20)
                .affectEntities(entities -> {
                    Flux<Player> flux = entities
                            .filter(entity -> entity instanceof Player)
                            .cast(Player.class)
                            .sort((o1, o2) -> {
                                Location first = o1.getLocation();
                                Location second = o2.getLocation();
                                return (int) (first.distanceSquared(this.location) - second.distanceSquared(this.location));
                            });
                    if (!this.global) flux = flux.filter(this.allowedViewers::contains);
                    flux.collectList()
                            .doOnNext(players -> { // calculate viewers
                                this.currentViewers.removeIf(player -> {
                                    if (players.contains(player) && player.isOnline()) return false;
                                    this.despawn(player);
                                    return true;
                                });
                                players.forEach(player -> {
                                    if (this.currentViewers.add(player)) {
                                        this.spawn(player);
                                    }
                                });
                            })
                            .doOnNext(players -> {
                                World world = this.location.getWorld();
                                if (this.destiny != null) { // MOVE
                                    ImmutableVector origin = ImmutableVector.of(this.location);
                                    CompletableFuture<Location> future = this.destiny.getT2();
                                    if (this.currentPath == null || this.location.distance(this.currentPath) < 0.2) {
                                        Iterator<PathPosition> iterator = this.destiny.getT1();
                                        if (!iterator.hasNext()) {
                                            this.destiny = null;
                                            this.currentPath = null;
                                            future.complete(this.location.clone());
                                            return;
                                        }
                                        do {
                                            this.currentPath = BukkitMapper.toLocation(iterator.next());
                                        } while (this.location.distance(this.currentPath) < 1 && iterator.hasNext());
                                    }
                                    ImmutableVector destiny = ImmutableVector.of(this.currentPath);
                                    ImmutableVector direction = destiny.subtract(origin).normalize();
                                    ImmutableVector next = origin.add(direction.multiply(this.speed));
                                    this.location = next.toLocation(world)
                                            .setDirection(this.type == EntityType.ENDER_DRAGON ?
                                                    direction.negative() : direction);
                                    boolean onGround = origin.getDistanceAboveGround(world, true) < 0.1;
                                    players.forEach(player -> this.teleport(player, onGround));
                                } else {
                                    if (this.gravity) { // GRAVITY
                                        ImmutableVector origin = ImmutableVector.of(this.location);
                                        double distanceAboveGround = origin.getDistanceAboveGround(world, true);
                                        if (distanceAboveGround >= 0.1) {
                                            ImmutableVector destiny = origin.subtract(new ImmutableVector(0d, distanceAboveGround, 0d));
                                            ImmutableVector direction = destiny.subtract(origin).normalize();
                                            double delta = this.speed * distanceAboveGround;
                                            double speed = FastMath.max(this.speed, FastMath.min(1, delta));
                                            origin = origin.add(direction.multiply(speed));
                                            this.location.set(origin.getX(), origin.getY(), origin.getZ());
                                            boolean onGround = origin.getDistanceAboveGround(world, true) < 0.1;
                                            players.forEach(player -> this.teleport(player, onGround));
                                        }
                                    }
                                    // LOOK
                                    ImmutableVector original = ImmutableVector.of(this.location);
                                    switch (this.lookType) {
                                        case CLOSEST_PLAYER -> {
                                            ImmutableVector destiny = ImmutableVector.of(players.get(0).getLocation());
                                            ImmutableVector direction = destiny.subtract(original).normalize();
                                            this.location.setDirection(this.type == EntityType.ENDER_DRAGON ?
                                                    direction.negative() : direction);
                                            players.forEach(player ->
                                                    this.lookAt(player, this.location.getYaw(), this.location.getPitch()));
                                        }
                                        case PER_PLAYER -> players.forEach(player -> {
                                            ImmutableVector destiny = ImmutableVector.of(player.getLocation());
                                            ImmutableVector direction = destiny.subtract(original).normalize();
                                            Location loc = this.location.clone().setDirection(this.type ==
                                                    EntityType.ENDER_DRAGON ? direction.negative() : direction);
                                            this.lookAt(player, loc.getYaw(), loc.getPitch());
                                        });
                                    }
                                }
                            })
                            .subscribe();
                });
    }

    public boolean show(Player player) {
        return this.allowedViewers.add(player);
    }

    public boolean hide(Player player) {
        return this.allowedViewers.remove(player);
    }

    public boolean canView(Player player) {
        if (this.global) return true;
        return this.allowedViewers.contains(player);
    }

    public boolean isDisplayed(Player player) {
        return this.currentViewers.contains(player);
    }

    public void lookAt(Player player, float yaw, float pitch) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.rotate(player, this, yaw, pitch));
    }

    public void teleport(Player player, Location location) {
        this.location = location;
        this.teleport(player, ImmutableVector.of(location)
                .getDistanceAboveGround(location.getWorld(), true) < 0.1);
    }

    private void teleport(Player player, boolean onGround) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.teleport(player, this, onGround));
    }

    public Mono<Location> moveTo(Location location) {
        return Mono.just(PatheticMapper.newPathfinder())
                .flatMap(pathfinder -> Mono.fromFuture(pathfinder.findPath(
                        BukkitMapper.toPathPosition(this.location),
                        BukkitMapper.toPathPosition(location),
                        this.pathfinderStrategy
                ).toCompletableFuture()))
                .filter(PathfinderResult::successful)
                .flatMap(result -> {
                    CompletableFuture<Location> future = new CompletableFuture<>();
                    this.destiny = Tuples.of(result.getPath().iterator(), future);
                    return Mono.fromFuture(future);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    this.currentViewers.forEach(player -> this.teleport(player, location));
                    return Mono.just(this.location).delayElement(Duration.ofSeconds(1));
                }));
    }

    protected void setTeam(Player player, TeamColor color) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.createTeam(player, this));
    }

    protected void spawn(Player player) {
        if (this.type == EntityType.PLAYER) {
            this.spawnPlayer(player);
        } else {
            this.spawnEntity(player);
        }
    }

    private void spawnPlayer(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.spawnPlayer(player, this));
    }

    private void spawnEntity(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.spawnEntity(player, this));
    }

    private void despawn(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.despawnEntity(player, this));
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public World getWorld() {
        return this.location.getWorld();
    }

    public void remove() {
        this.currentViewers.forEach(this::despawn);
    }

    public interface PacketEntityInteractHandler {
        void handle(Player player, ClickType clickType);
    }

}
