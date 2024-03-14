package dev.ckateptb.minecraft.jyraf.packet.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.LookType;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.team.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class PacketEntity {
    private final static CachedReference<PlayerManager> PACKET_MANAGER = new CachedReference<>(() ->
            PacketEvents.getAPI().getPlayerManager());
    private final int id;
    @Getter
    @Setter
    private boolean global = true; // means that any player can see the entity
    @Getter
    private final UUID uniqueId;
    private final EntityType type;
    private final Set<Player> allowedViewers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    private final Map<PacketEntityProperty<?>, Object> properties = new HashMap<>();
    private Location location;
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
                                return (int) (first.distanceSquared(location) - second.distanceSquared(location));
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
                                } else if (this.gravity) { // GRAVITY
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
                                } else { // LOOK
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

    @SuppressWarnings("unchecked")
    public <T> T getProperty(PacketEntityProperty<T> key) {
        return (T) this.properties.getOrDefault(key, key.getDefaultValue());
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

    public void lookAt(Player player, float yaw, float pitch) {
        this.sendPacket(player, new WrapperPlayServerEntityHeadLook(this.id, yaw));
        this.sendPacket(player, new WrapperPlayServerEntityRotation(this.id, yaw, pitch, true));
    }

    public void equip(Player player, Equipment equipment) {
        this.sendPacket(player, new WrapperPlayServerEntityEquipment(this.id, Collections.singletonList(equipment)));
    }

    public void setTeam(Player player, TeamColor color) {
        String team = "npc_team_" + this.id;
        this.sendPacket(player, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.CREATE,
                new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                        Text.of(" "), null, null,
                        WrapperPlayServerTeams.NameTagVisibility.NEVER,
                        WrapperPlayServerTeams.CollisionRule.NEVER,
                        color == null ? NamedTextColor.WHITE : NamedTextColor.NAMES.value(color.name().toLowerCase()),
                        WrapperPlayServerTeams.OptionData.NONE
                )));
        this.sendPacket(player, new WrapperPlayServerTeams(
                team, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                this.type == EntityType.PLAYER ? Integer.toString(this.id) : this.uniqueId.toString()));
    }

    public void removeTeam(Player player) {
        this.sendPacket(player, new WrapperPlayServerTeams("npc_team_" + this.id,
                WrapperPlayServerTeams.TeamMode.REMOVE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null));
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
                    System.out.println(result.getPath().length());
                    for (PathPosition path : result.getPath()) {
                        Location l = BukkitMapper.toLocation(path);
                        this.currentViewers.forEach(player -> player.sendBlockChange(l, Material.ROSE_BUSH.createBlockData()));
                    }
                    CompletableFuture<Location> future = new CompletableFuture<>();
                    this.destiny = Tuples.of(result.getPath().iterator(), future);
                    return Mono.fromFuture(future);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    this.currentViewers.forEach(player -> this.teleport(player, location));
                    return Mono.just(this.location).delayElement(Duration.ofSeconds(1));
                }));
    }

    private void spawn(Player player) {
        boolean isPlayer = this.type == EntityType.PLAYER;
        if (isPlayer) {
            String name = "npc-" + this.id;
            UserProfile profile = new UserProfile(this.uniqueId, name);
            // TODO Skin
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    profile, false, 1, GameMode.CREATIVE, Text.of(name), null
            );
            this.sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(
                    WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
            ), info, info));
        }
        float yaw = this.location.getYaw();
        this.sendPacket(player, new WrapperPlayServerSpawnEntity(this.id, this.uniqueId,
                SpigotConversionUtil.fromBukkitEntityType(this.type),
                SpigotConversionUtil.fromBukkitLocation(this.location),
                yaw, 0, new Vector3d()
        ));
        if (isPlayer) {
            this.lookAt(player, yaw, this.location.getPitch());
            // TODO Send Metadata
        }
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

    private void sendPacket(Player player, PacketWrapper<?> packet) {
        PACKET_MANAGER.get().ifPresent(playerManager -> playerManager.sendPacket(player, packet));
    }
}
