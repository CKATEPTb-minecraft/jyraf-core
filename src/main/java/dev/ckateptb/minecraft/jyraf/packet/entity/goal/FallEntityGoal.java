package dev.ckateptb.minecraft.jyraf.packet.entity.goal;

import dev.ckateptb.minecraft.jyraf.math.ImmutableVector;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.packet.property.Property;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FallEntityGoal extends PacketGoal<PacketEntity> {
    public FallEntityGoal(Priority priority) {
        super(priority);
    }

    @Override
    public Result onTick(PacketEntity entry, Player... players) {
        Location location = entry.getLocation();
        ImmutableVector origin = ImmutableVector.of(location);
        World world = location.getWorld();
        double distanceAboveGround = origin.getDistanceAboveGround(world, true);
        if (distanceAboveGround >= 0.1) {
            ImmutableVector destiny = origin.subtract(new ImmutableVector(0d, distanceAboveGround, 0d));
            ImmutableVector direction = destiny.subtract(origin).normalize();
            Double entitySpeed = Property.ENTITY_SPEED.parse(entry, Double.class);
            double delta = entitySpeed * distanceAboveGround;
            double speed = FastMath.max(entitySpeed, FastMath.min(1, delta));
            origin = origin.add(direction.multiply(speed));
            location.set(origin.getX(), origin.getY(), origin.getZ());
            entry.teleport(location, Arrays.asList(players));
        }
        return Result.CONTINUE;
    }
}
