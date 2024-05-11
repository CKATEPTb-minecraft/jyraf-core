package dev.ckateptb.minecraft.jyraf.packet.entity.goal;

import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketHologram;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class NameTagGoal extends PacketGoal<PacketEntity> {
    private final double yOffset;
    private final LazyLoader.Later<PacketHologram> hologram = LazyLoader.later();
    private final Collection<Component> components;

    public NameTagGoal(double yOffset, Collection<Component> components) {
        super(Priority.LOWEST);
        this.yOffset = yOffset;
        this.components = components;
    }

    @Override
    public void onSpawn(PacketEntity entry, Player... players) {
        if (!this.hologram.isDefined()) {
            this.hologram.defer(() -> PacketEntity.hologram(entry.getLocation().add(0, yOffset, 0), components));
        }
        this.hologram.get().spawn(Arrays.asList(players));
    }

    @Override
    public void onTeleport(PacketEntity entry, Location location, Player... players) {
        if(this.hologram.isDefined()) {
            this.hologram.get().teleport(location.add(0, this.yOffset, 0), Arrays.asList(players));
        }
    }

    @Override
    public void onVelocity(PacketEntity entry, Vector vector, Player... players) {
        if (this.hologram.isDefined()) {
            this.hologram.get().velocity(vector, Arrays.asList(players));
        }
    }

    @Override
    public void onDespawn(PacketEntity entry, Player... players) {
        if (this.hologram.isDefined()) {
            this.hologram.get().despawn(Arrays.asList(players));
        }
    }
}
