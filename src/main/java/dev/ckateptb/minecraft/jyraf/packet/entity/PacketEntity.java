package dev.ckateptb.minecraft.jyraf.packet.entity;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.basic.Interactable;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patheloper.api.pathing.result.PathfinderResult;
import org.patheloper.api.pathing.strategy.PathfinderStrategy;
import org.patheloper.api.pathing.strategy.strategies.DirectPathfinderStrategy;
import org.patheloper.api.wrapper.PathPosition;
import org.patheloper.mapping.PatheticMapper;
import org.patheloper.mapping.bukkit.BukkitMapper;
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
@Getter
public class PacketEntity extends Interactable<PacketEntity> {
    protected final int id;
    @NotNull
    protected final UUID uniqueId;
    @NotNull
    protected final EntityType type;
    @Setter
    @NotNull
    private LookType lookType = LookType.FIXED;
    @Setter
    private boolean gravity = false;
    @Setter
    private double speed = 0.2;
    @Setter
    @NotNull
    private PathfinderStrategy pathfinderStrategy = new DirectPathfinderStrategy();
    @Setter
    @Nullable
    private Tuple2<Iterator<PathPosition>, CompletableFuture<Location>> destiny = null;
    @Setter
    @Nullable
    private Location currentPath;
    @Setter
    @NotNull
    private TeamColor teamColor = TeamColor.WHITE;

    public PacketEntity(int id, EntityType type, Location location) {
        this(id, UUID.randomUUID(), type, location, true);
    }

    public PacketEntity(int id, @NotNull UUID uniqueId, @NotNull EntityType type, Location location, boolean global) {
        this(id, uniqueId, type, location, new ArrayList<>());
        this.global = global;
    }

    public PacketEntity(int id, @NotNull UUID uniqueId, @NotNull EntityType type, Location location, Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        Objects.requireNonNull(uniqueId);
        Objects.requireNonNull(type);
        this.id = id;
        this.uniqueId = uniqueId;
        this.type = type;
        addTrait(new NPCMoveTrait());
        addTrait(new NPCGravityTrait());
        addTrait(new NPCLookTrait());
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
    }

    @Override
    public void destroy(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.despawnEntity(player, this));
    }

    static final class NPCLookTrait extends PacketTrait<PacketEntity> {

        public NPCLookTrait() {
            super(EventPriority.MONITOR);
        }

        @Override
        public @NotNull Class<PacketEntity> getEntryClass() {
            return PacketEntity.class;
        }

        @Override
        public void tick(@NotNull Mono<List<Player>> playersMono, @NotNull PacketEntity entity) {
            playersMono.doOnNext(players -> {
                        if (entity.destiny != null) return;
                        ImmutableVector original = ImmutableVector.of(entity.location);
                        switch (entity.lookType) {
                            case CLOSEST_PLAYER -> {
                                ImmutableVector destiny = ImmutableVector.of(players.get(0).getLocation());
                                ImmutableVector direction = destiny.subtract(original).normalize();
                                entity.location.setDirection(entity.type == EntityType.ENDER_DRAGON ?
                                        direction.negative() : direction);
                                players.forEach(player ->
                                        entity.lookAt(player, entity.location.getYaw(), entity.location.getPitch()));
                            }
                            case PER_PLAYER -> players.forEach(player -> {
                                ImmutableVector destiny = ImmutableVector.of(player.getLocation());
                                ImmutableVector direction = destiny.subtract(original).normalize();
                                Location loc = entity.location.clone().setDirection(entity.type ==
                                        EntityType.ENDER_DRAGON ? direction.negative() : direction);
                                entity.lookAt(player, loc.getYaw(), loc.getPitch());
                            });
                        }
                    })
                    .subscribe();
        }
    }

    static final class NPCMoveTrait extends PacketTrait<PacketEntity> {

        public NPCMoveTrait() {
            super(EventPriority.MONITOR);
        }

        @Override
        public @NotNull Class<PacketEntity> getEntryClass() {
            return PacketEntity.class;
        }

        @Override
        public void tick(@NotNull Mono<List<Player>> playersMono, @NotNull PacketEntity entity) {
            World world = entity.location.getWorld();
            playersMono.doOnNext(players -> {
                        if (entity.destiny == null) return;
                        ImmutableVector origin = ImmutableVector.of(entity.location);
                        CompletableFuture<Location> future = entity.destiny.getT2();
                        if (entity.currentPath == null || entity.location.distance(entity.currentPath) < 0.2) {
                            Iterator<PathPosition> iterator = entity.destiny.getT1();
                            if (!iterator.hasNext()) {
                                entity.destiny = null;
                                entity.currentPath = null;
                                future.complete(entity.location.clone());
                                return;
                            }
                            do {
                                entity.currentPath = BukkitMapper.toLocation(iterator.next());
                            } while (entity.location.distance(entity.currentPath) < 1 && iterator.hasNext());
                        }
                        ImmutableVector destiny = ImmutableVector.of(entity.currentPath);
                        ImmutableVector direction = destiny.subtract(origin).normalize();
                        ImmutableVector next = origin.add(direction.multiply(entity.speed));
                        entity.location = next.toLocation(world)
                                .setDirection(entity.type == EntityType.ENDER_DRAGON ?
                                        direction.negative() : direction);
                        boolean onGround = origin.getDistanceAboveGround(world, true) < 0.1;
                        players.forEach(player -> entity.teleport(player, onGround));
                    })
                    .subscribe();
        }
    }

    static final class NPCGravityTrait extends PacketTrait<PacketEntity> {

        public NPCGravityTrait() {
            super(EventPriority.MONITOR);
        }

        @Override
        public @NotNull Class<PacketEntity> getEntryClass() {
            return PacketEntity.class;
        }

        @Override
        public void tick(@NotNull Mono<List<Player>> playersMono, @NotNull PacketEntity entity) {
            playersMono.doOnNext(players -> {
                        if (!entity.gravity || entity.destiny != null) return;
                        ImmutableVector origin = ImmutableVector.of(entity.location);
                        double distanceAboveGround = origin.getDistanceAboveGround(entity.getWorld(), true);
                        if (distanceAboveGround >= 0.1) {
                            ImmutableVector destiny = origin.subtract(new ImmutableVector(0d, distanceAboveGround, 0d));
                            ImmutableVector direction = destiny.subtract(origin).normalize();
                            double delta = entity.speed * distanceAboveGround;
                            double speed = FastMath.max(entity.speed, FastMath.min(1, delta));
                            origin = origin.add(direction.multiply(speed));
                            entity.location.set(origin.getX(), origin.getY(), origin.getZ());
                            boolean onGround = origin.getDistanceAboveGround(entity.getWorld(), true) < 0.1;
                            players.forEach(player -> entity.teleport(player, onGround));
                        }
                    })
                    .subscribe();
        }
    }

}
