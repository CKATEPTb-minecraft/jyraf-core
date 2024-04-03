package dev.ckateptb.minecraft.jyraf.colider;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

public interface Collider {
    @NotNull Collider at(@NotNull Vector center);

    @NotNull Collider scale(double amount);

    @NotNull ImmutableVector getHalfExtents();

    <RT extends Collider> boolean intersects(@NotNull RT collider);

    boolean contains(@NotNull Vector vector);

    @NotNull Collider affectEntities(Consumer<Flux<Entity>> consumer);

    @NotNull Collider affectBlocks(@NotNull Consumer<Flux<Block>> consumer);

    @NotNull Collider affectLocations(@NotNull Consumer<Flux<Location>> consumer);

    @NotNull Collider grow(Vector vector);

    @NotNull World getWorld();

    @NotNull ImmutableVector getCenter();

    @SuppressWarnings("unchecked")
    @NotNull
    default <T extends Collider> T at(Location location) {
        return (T) this.at(ImmutableVector.of(location));
    }
}