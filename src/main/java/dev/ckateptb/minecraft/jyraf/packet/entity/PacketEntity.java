package dev.ckateptb.minecraft.jyraf.packet.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Bukkit;
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
//  Interaction Handler, Equipments, Poses, States
//  Dropped Item, Item Display, Block Display, Text Display
//  Implement 1.16.5 support
public class PacketEntity {
    private final static CachedReference<PlayerManager> PACKET_MANAGER = new CachedReference<>(() ->
            PacketEvents.getAPI().getPlayerManager());
    protected final int id;
    @Getter
    protected final UUID uniqueId;
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
                                    if (players.contains(player)) return false;
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

    public void show(Player player) {
        this.allowedViewers.add(player);
    }

    public void hide(Player player) {
        this.allowedViewers.remove(player);
    }

    public boolean canView(Player player) {
        if (this.global) return true;
        return this.allowedViewers.contains(player);
    }

    public boolean isDisplayed(Player player) {
        return this.currentViewers.contains(player);
    }

    public void lookAt(Player player, float yaw, float pitch) {
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(this.id, yaw));
        this.sendPacket(player, new WrapperPlayServerEntityRotation(this.id, yaw, pitch, true));
    }

    public void teleport(Player player, Location location) {
        boolean ground = ImmutableVector.of(location).getDistanceAboveGround(location.getWorld(), true) < 0.1;
        this.location = location;
        this.sendPacket(player, new WrapperPlayServerEntityTeleport(this.id, SpigotConversionUtil.fromBukkitLocation(location), ground));
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(this.id, location.getYaw()));
    }

    private void teleport(Player player, boolean onGround) {
        this.sendPacket(player, new WrapperPlayServerEntityTeleport(this.id,
                SpigotConversionUtil.fromBukkitLocation(this.location), onGround));
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(this.id, this.location.getYaw()));
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
        String team = "npc-team-" + this.id;
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.REMOVE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null));
        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Text.of(" "), null, null,
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                Optional.ofNullable(color).orElse(TeamColor.WHITE).getKyori(),
                WrapperPlayServerTeams.OptionData.NONE
        );
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.CREATE, info));
        String id = this.type == EntityType.PLAYER ? Integer.toString(this.id) : this.getUniqueId().toString();
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, id));
    }

    protected void spawn(Player player) {
        if (this.type == EntityType.PLAYER) {
            this.spawnPlayer(player);
        } else {
            this.spawnEntity(player);
            this.setTeam(player, TeamColor.WHITE);
        }
    }

    private void spawnPlayer(Player player) {
        UserProfile profile = new UserProfile(this.uniqueId, Integer.toString(this.id));
        Skin.from(player) // TODO parse from properties
                .doOnNext(profile::setTextureProperties)
                .doFinally(signalType -> Bukkit.getScheduler()
                        .runTaskLaterAsynchronously(Jyraf.getPlugin(), () ->
                                this.sendPacket(player, new WrapperPlayServerPlayerInfoRemove(this.uniqueId)), 60)
                )
                .subscribe(textureProperty -> {
                    WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                            profile, false, 1, GameMode.CREATIVE, Text.of("npc-" + this.id), null
                    );
                    this.sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(
                            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                    ), info, info));
                    this.spawnEntity(player);
                    this.lookAt(player, this.location.getYaw(), this.location.getPitch());
                    this.setTeam(player, TeamColor.GOLD);
                });
    }

    private void spawnEntity(Player player) {
        this.sendPacket(player, new WrapperPlayServerSpawnEntity(this.id, this.uniqueId,
                SpigotConversionUtil.fromBukkitEntityType(this.type),
                SpigotConversionUtil.fromBukkitLocation(this.location),
                this.location.getYaw(), 0, new Vector3d()
        ));
    }

    private void despawn(Player player) {
        this.sendPacket(player, new WrapperPlayServerDestroyEntities(this.id));
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public World getWorld() {
        return this.location.getWorld();
    }

    protected void sendPacket(Player player, PacketWrapper<?> packet) {
        PACKET_MANAGER.get().ifPresent(playerManager -> playerManager.sendPacket(player, packet));
    }
}
