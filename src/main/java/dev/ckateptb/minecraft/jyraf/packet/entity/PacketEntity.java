package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.basic.Interactable;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TODO Implement properties like a
//  skin, glow, entity type data (villager type etc.)
//  Holograms, Multiple NameTags (based on Holograms)
//  Equipments, Poses, States
//  Dropped Item, Item Display, Block Display, Text Display
//  Implement 1.16.5 support
public class PacketEntity extends Interactable {
    @Getter
    protected final int id;
    @Getter
    protected final UUID uniqueId;
    @Getter
    protected final EntityType type;
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

    public PacketEntity(int id, EntityType type, Location location) {
        this(id, UUID.randomUUID(), type, location);
    }

    public PacketEntity(int id, UUID uniqueId, EntityType type, Location location) {
        super(location, true);
        this.id = id;
        this.uniqueId = uniqueId;
        this.type = type;
    }

    public PacketEntity(int id, UUID uniqueId, EntityType type, Location location, Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        this.id = id;
        this.uniqueId = uniqueId;
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
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
                    Mono<List<Player>> mono = flux.collectList();
                    // todo: move it to trait i think
                    mono.doOnNext(players -> {
                                this.currentViewers.removeIf(player -> {
                                    if (players.contains(player) && player.isOnline()) return false;
                                    this.destroy(player);
                                    return true;
                                });
                                players.forEach(player -> {
                                    if (this.currentViewers.add(player)) {
                                        this.display(player);
                                    }
                                });
                            })
                            .subscribe();
                    mono.doOnNext(players -> {
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
//                    for (PacketTrait<?> unknownTrait : this.getTraits()) {
//                        if (unknownTrait.getEntryClass() != PacketBlock.class) continue;
//                        PacketTrait<PacketEntity> trait = (PacketTrait<PacketEntity>) unknownTrait;
//                        if (trait.isCancelled()) continue;
//                        mono.doOnNext(players -> players.forEach(player -> trait.tick(player, this))).subscribe();
//                    }
                });
    }

    public void lookAt(Player player, float yaw, float pitch) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.rotate(player, this, yaw, pitch));
    }

    public void teleport(Location location) {
        this.location = location;
        this.currentViewers.forEach(player -> this.teleport(player, location));
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

    private void spawnPlayer(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.spawnPlayer(player, this));
    }

    private void spawnEntity(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.spawnEntity(player, this));
    }

    @Override
    public @NotNull Location getLocation() {
        return this.location.clone();
    }

    @Override
    public void display(Player player) {
        if (this.type == EntityType.PLAYER) this.spawnPlayer(player);
        else this.spawnEntity(player);
        player.sendMessage("shown entity to u!");
    }

    @Override
    public void destroy(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.despawnEntity(player, this));
        player.sendMessage("hidden entity from u!");
    }

}
