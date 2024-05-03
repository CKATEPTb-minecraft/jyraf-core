package dev.ckateptb.minecraft.jyraf.packet.entity.goal;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class LookEntityGoal extends PacketGoal<PacketEntity> {
    private final Mode mode;

    public LookEntityGoal(Mode mode) {
        this(Priority.NORMAL, mode);
    }

    public LookEntityGoal(Priority priority, Mode mode) {
        super(priority);
        this.mode = mode;
    }

    @Override
    public Result onTick(PacketEntity entry, Player... players) {
        Location location = entry.getLocation();
        ImmutableVector original = ImmutableVector.of(location);
        EntityType type = entry.getType();
        switch (this.mode) {
            case CLOSEST_PLAYER -> {
                ImmutableVector destiny = ImmutableVector.of(players[0].getLocation());
                ImmutableVector direction = destiny.subtract(original).normalize();
                location.setDirection(type == EntityType.ENDER_DRAGON ? direction.negative() : direction);
                entry.rotate(location.getYaw(), location.getPitch(), List.of(players));
            }
            case PER_PLAYER -> {
                for (Player player : players) {
                    ImmutableVector destiny = ImmutableVector.of(player.getLocation());
                    ImmutableVector direction = destiny.subtract(original).normalize();
                    location.setDirection(type == EntityType.ENDER_DRAGON ? direction.negative() : direction);
                    entry.rotate(location.getYaw(), location.getPitch(), List.of(player));
                }
            }
        }
        return Result.CONTINUE;
    }

    public enum Mode {
        CLOSEST_PLAYER,
        PER_PLAYER
    }
}
