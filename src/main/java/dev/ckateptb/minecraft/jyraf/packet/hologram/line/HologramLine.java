package dev.ckateptb.minecraft.jyraf.packet.hologram.line;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class HologramLine {

    protected final UUID uniqueId;
    private final PacketEntity entity;
    @Getter
    private boolean global = true;
    @Getter
    protected Location location;
    // todo
    @Getter
    @Setter
    private Object interactHandler = null;

    public HologramLine(Location location) {
        this(UUID.randomUUID(), location, EntityType.ARMOR_STAND);
    }

    public HologramLine(Location location, EntityType type) {
        this(UUID.randomUUID(), location, type);
    }

    public HologramLine(UUID uniqueId, Location location, EntityType type) {
        this.uniqueId = uniqueId;
        this.location = location;
        this.entity = new PacketEntity(SpigotReflectionUtil.generateEntityId(), type, location);
    }

    public boolean show(Player player) {
        return this.entity.show(player);
    }

    public boolean hide(Player player) {
        return this.entity.hide(player);
    }

    public boolean canView(Player player) {
        return this.entity.canView(player);
    }

    public boolean isDisplayed(Player player) {
        return this.entity.isDisplayed(player);
    }

    public void setGlobal(boolean global) {
        if (this.global == global) return;
        this.global = global;
        this.entity.setGlobal(global);
    }

    public void setLocation(Location location) {
        if (location == null) {
            this.remove();
            return;
        }
        if (location.equals(this.location)) return;
        this.location = location;
        this.entity.teleport(location);
    }

    public void remove() {
        this.global = false;
        this.entity.remove();
    }

}