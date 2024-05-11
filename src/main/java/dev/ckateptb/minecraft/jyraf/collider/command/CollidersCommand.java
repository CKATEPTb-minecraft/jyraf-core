package dev.ckateptb.minecraft.jyraf.collider.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.destroystokyo.paper.ParticleBuilder;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.collider.Collider;
import dev.ckateptb.minecraft.jyraf.collider.Colliders;
import dev.ckateptb.minecraft.jyraf.collider.geometry.OrientedBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.collider.geometry.SphereBoundingBoxCollider;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// TODO Remove. We don't need debugging commands in production
@Getter
@Component
@RequiredArgsConstructor
public class CollidersCommand implements Command {
    private final Jyraf plugin;
    private final Set<Collider<?>> colliders = new HashSet<>();

    @CommandMethod("colliders aabb <x> <y> <z> ")
    @CommandPermission("colliders.admin")
    public void aabb(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector half = new ImmutableVector(x, y, z);
        this.colliders.add(Colliders.aabb(player.getLocation(), half));
    }

    @CommandMethod("colliders sphere <radius>")
    @CommandPermission("colliders.admin")
    public void sphere(Player player, @Argument("radius") Double radius) {
        this.colliders.add(Colliders.sphere(player.getLocation(), radius));
    }

    @CommandMethod("colliders obb <x> <y> <z>")
    @CommandPermission("colliders.admin")
    public void obb(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector half = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, 0).radians().toEulerAngle();
        this.colliders.add(Colliders.obb(location, half, eulerAngle));
    }

    @CommandMethod("colliders ray <distance> <size>")
    @CommandPermission("colliders.admin")
    public void ray(Player player, @Argument("distance") Double distance, @Argument("size") Double size) {
        this.colliders.add(Colliders.ray(player, distance, size));
    }

    @CommandMethod("colliders disc <x> <y> <z>")
    @CommandPermission("colliders.admin")
    public void disk(Player player, @Argument("x") Double x, @Argument("y") Double y, @Argument("z") Double z) {
        ImmutableVector half = new ImmutableVector(x, y, z);
        Location location = player.getLocation();
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        float roll = 0;
        EulerAngle eulerAngle = new ImmutableVector(pitch, yaw, roll).radians().toEulerAngle();
        OrientedBoundingBoxCollider obb = Colliders.obb(location, half, eulerAngle);
        SphereBoundingBoxCollider sphere = Colliders.sphere(player.getLocation(), half.maxComponent() * 0.75);
        this.colliders.add(Colliders.disk(obb, sphere));
    }

    @CommandMethod("colliders clear")
    @CommandPermission("colliders.admin")
    public void clearStatic() {
        this.colliders.clear();
    }

    @Schedule(async = true, fixedRate = 5, initialDelay = 0)
    public void tick() {
        Collider<?>[] colliders = this.colliders.toArray(Collider[]::new);
        for (Collider<?> collider : colliders) {
            collider.findEntities()
                    .filter(entity -> entity instanceof LivingEntity)
                    .cast(LivingEntity.class)
                    .publishOn(Jyraf.getPlugin().syncScheduler())
                    .subscribe(entity -> {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5, 1));
                    });
            collider.getDraw().get()
                    .stream()
                    .map(vector -> vector.toLocation(collider.getWorld()))
                    .forEach(location -> {
                        ParticleBuilder particle = Particle.REDSTONE.builder().force(true).location(location).count(1);
                        if (Arrays.stream(colliders)
                                .filter(other -> other != collider)
                                .anyMatch(other -> other.intersects(collider))) {
                            particle.color(Color.RED, 3.5f).spawn();
                        } else {
                            particle.color(Color.GREEN, 3.5f).spawn();
                        }
                    });
        }
    }
}
