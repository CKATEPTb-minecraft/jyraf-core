package dev.ckateptb.minecraft.jyraf.repository.entity;

import dev.ckateptb.minecraft.jyraf.repository.Repository;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import reactor.core.publisher.Flux;

public interface EntityRepository extends Repository<Entity> {
    Flux<Entity> getNearbyEntities(Location location, double radius);
}
