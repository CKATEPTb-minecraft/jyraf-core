package dev.ckateptb.minecraft.jyraf.packet.hologram.line.implementation;

import dev.ckateptb.minecraft.jyraf.packet.hologram.line.HologramLine;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class HologramTextLine extends HologramLine {

    public HologramTextLine(Location location) {
        super(location);
    }

    public HologramTextLine(Location location, EntityType type) {
        super(location, type);
    }

    public HologramTextLine(UUID uniqueId, Location location, EntityType type) {
        super(uniqueId, location, type);
    }

}