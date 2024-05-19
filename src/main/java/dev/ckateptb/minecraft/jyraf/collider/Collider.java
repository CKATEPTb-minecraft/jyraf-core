package dev.ckateptb.minecraft.jyraf.collider;

import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import dev.ckateptb.minecraft.jyraf.repository.entity.EntityRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.ChunkRepository;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Collider<T extends Collider<T>> {
    LazyLoader<Mono<WorldRepositoryService>> SERVICE = LazyLoader.of(() -> IoC.getBean(WorldRepositoryService.class).orElseGet(Mono::empty));

    @NotNull
    T at(@NotNull Location location);

    @Nullable
    T scale(double multiplier);

    @NotNull
    T grow(Vector size);

    @NotNull
    Vector getHalfExtents();

    default boolean intersects(@NotNull Collider<?> other) {
        return Colliders.findIntersect(this.getClass(), other.getClass()).intersect(this, other);
    }

    boolean contains(@NotNull Vector vector);

    @NotNull
    Location getLocation();

    LazyLoader<Collection<ImmutableVector>> getDraw();

    default World getWorld() {
        return this.getLocation().getWorld();
    }

    @NotNull
    default Flux<Entity> findEntities() {
        World world = this.getWorld();
        Location location = this.getLocation();
        double radius = ImmutableVector.of(this.getHalfExtents()).maxComponent();
        double finalRadius = FastMath.max(radius, FastMath.min(radius * 2, 9));
        return SERVICE.get()
                .flatMap(service -> service.getRepository(Entity.class, world))
                .cast(EntityRepository.class)
                .flatMapMany(repository -> repository.getNearbyEntities(location, finalRadius))
                .filter(entity -> this.intersects(Colliders.aabb(entity)));
    }


    @NotNull
    default Flux<Block> findBlocks() {
        World world = this.getWorld();
        Location location = this.getLocation();
        Vector halfExtents = this.getHalfExtents();
        Location min = location.clone().subtract(halfExtents);
        Location max = location.clone().add(halfExtents);
        List<Location> locations = new ArrayList<>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    locations.add(new Location(world, x, y, z));
                }
            }
        }
        return Flux.fromIterable(locations)
                .map(Location::getBlock)
                .filter(block -> this.intersects(Colliders.aabb(block)));
    }
}