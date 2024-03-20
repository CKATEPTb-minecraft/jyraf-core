package dev.ckateptb.minecraft.jyraf.colider.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.destroystokyo.paper.ParticleBuilder;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.colider.Collider;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.colider.geometry.OrientedBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.colider.geometry.SphereBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Getter
//@Component
@RequiredArgsConstructor
public class CollidersCommand implements Command {
    private final Jyraf plugin;
    private final Set<Collider> colliders = new HashSet<>();

    @CommandMethod("colliders debug direct aabb <x> <y> <z> [duration]")
    @CommandPermission("colliders.admin")
    public void aabbDirect(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z, @Argument("duration") Long duration) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        this.renderDirect(
                Colliders.aabb(player.getWorld(), immutableVector.negative(), immutableVector),
                player,
                immutableVector.maxComponent() + 3
                , duration
        );
    }

    @CommandMethod("colliders debug static aabb <x> <y> <z>")
    @CommandPermission("colliders.admin")
    public void aabbStatic(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        colliders.add(Colliders.aabb(player.getWorld(), immutableVector.negative(), immutableVector).at(player.getLocation()));
    }

    @CommandMethod("colliders debug direct sphere <radius> [duration]")
    @CommandPermission("colliders.admin")
    public void sphereDirect(Player player, @Argument("radius") Double radius, @Argument("duration") Long duration) {
        this.renderDirect(
                Colliders.sphere(player.getWorld(), ImmutableVector.ZERO, radius),
                player,
                radius + 3,
                duration
        );
    }

    @CommandMethod("colliders debug static sphere <radius>")
    @CommandPermission("colliders.admin")
    public void sphereStatic(Player player, @Argument("radius") Double radius) {
        colliders.add(Colliders.sphere(player.getWorld(), ImmutableVector.ZERO, radius).at(player.getLocation()));
    }

    @CommandMethod("colliders debug direct obb <x> <y> <z> [duration]")
    @CommandPermission("colliders.admin")
    public void obbDirect(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z, @Argument("duration") Long duration) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        this.renderDirect(
                Colliders.obb(player.getWorld(), ImmutableVector.ZERO, immutableVector, eulerAngle),
                player,
                immutableVector.maxComponent() + 3,
                duration
        );
    }

    @CommandMethod("colliders debug static obb <x> <y> <z>")
    @CommandPermission("colliders.admin")
    public void obbStatic(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        colliders.add(Colliders.obb(player.getWorld(), ImmutableVector.ZERO, immutableVector, eulerAngle).at(location));
    }

    @CommandMethod("colliders debug direct ray <distance> <size> [duration]")
    @CommandPermission("colliders.admin")
    public void rayDirect(Player player, @Argument("distance") Double distance, @Argument("size") Double size, @Argument("duration") Long duration) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        this.renderDirect(
                Colliders.ray(player.getWorld(), eyeLocation.toVector(), direction, distance, size),
                player,
                0d,
                duration
        );
    }

    @CommandMethod("colliders debug static ray <distance> <size>")
    @CommandPermission("colliders.admin")
    public void rayStatic(Player player, @Argument("distance") Double distance, @Argument("size") Double size) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        colliders.add(Colliders.ray(player.getWorld(), ImmutableVector.ZERO, direction, distance, size).at(eyeLocation));
    }

    @CommandMethod("colliders debug direct disc <x> <y> <z> [duration]")
    @CommandPermission("colliders.admin")
    public void discDirect(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z, @Argument("duration") Long duration) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        World world = player.getWorld();
        OrientedBoundingBoxCollider obb = Colliders.obb(world, ImmutableVector.ZERO, immutableVector, eulerAngle);
        SphereBoundingBoxCollider sphere = Colliders.sphere(world, ImmutableVector.ZERO, immutableVector.maxComponent() * 0.75);
        this.renderDirect(
                Colliders.disk(world, obb, sphere),
                player,
                immutableVector.maxComponent() + 3,
                duration
        );
    }

    @CommandMethod("colliders debug static disc <x> <y> <z>")
    @CommandPermission("colliders.admin")
    public void discStatic(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector immutableVector = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        World world = player.getWorld();
        OrientedBoundingBoxCollider obb = Colliders.obb(world, ImmutableVector.ZERO, immutableVector, eulerAngle);
        SphereBoundingBoxCollider sphere = Colliders.sphere(world, ImmutableVector.ZERO, immutableVector.maxComponent() * 0.75);
        colliders.add(Colliders.disk(world, obb, sphere).at(location));
    }

    @CommandMethod("colliders debug static clear")
    @CommandPermission("colliders.admin")
    public void clearStatic() {
        colliders.clear();
    }

    @Schedule(async = true, fixedRate = 5, initialDelay = 0)
    public void renderStatic() {
        Collider[] colliders = this.colliders.toArray(Collider[]::new);
        for (Collider collider : colliders) {
            collider.affectEntities(flux -> flux
                    .publishOn(plugin.syncScheduler())
                    .subscribe(entity -> {
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5, 1));
                        }
                    }));
            collider.affectLocations(flux -> flux
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(location -> {
                        ParticleBuilder particle = Particle.REDSTONE.builder().force(true).location(location).count(1);
                        if (Arrays.stream(colliders).anyMatch(other -> {
                            if (collider == other) return false;
                            return other.contains(location.toVector());
                        })) {
                            particle.color(Color.RED, 3.5f).spawn();
                        } else if (Arrays.stream(colliders).anyMatch(other -> {
                            if (collider == other) return false;
                            return other.intersects(collider);
                        })) {
                            particle.color(Color.BLUE, 0.5f).spawn();
                        } else {
                            particle.color(Color.GREEN, 0.5f).spawn();
                        }
                    }));
        }
    }

    private void renderDirect(Collider collider, Player player, Double distance, Long duration) {
        if (duration == null) {
            collider.at(getCenter(distance, player))
                    .affectLocations(flux -> flux
                            .map(Location::getBlock)
                            .concatMap(block -> Mono.just(block).delayElement(Duration.of(1, ChronoUnit.MILLIS)))
                            .publishOn(plugin.syncScheduler())
                            .subscribe(block -> block.setType(Material.SAND, false)));
        } else {
            AtomicReference<Collider> colliderReference = new AtomicReference<>(collider);
            Disposable disposable = Schedulers.boundedElastic().schedulePeriodically(() -> {
                if (collider instanceof OrientedBoundingBoxCollider) {
                    Location location = player.getLocation();
                    float pitch = location.getPitch();
                    float yaw = location.getYaw();
                    float roll = 0;
                    EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
                    colliderReference.set(Colliders.obb(
                            collider.getWorld(),
                            collider.getCenter(),
                            collider.getHalfExtents(),
                            eulerAngle));
                }
                colliderReference.get().at(getCenter(distance, player))
                        .affectLocations(flux -> flux.subscribe(location ->
                                Particle.REDSTONE.builder().force(true).location(location)
                                        .count(1).color(Color.RED, 0.5f).spawn()));
            }, 0, 200, TimeUnit.MILLISECONDS);
            Schedulers.single().schedule(disposable::dispose, duration, TimeUnit.MILLISECONDS);
        }
    }

    private ImmutableVector getCenter(double distance, Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().multiply(distance);
        return ImmutableVector.of(eyeLocation.add(direction));
    }
}
