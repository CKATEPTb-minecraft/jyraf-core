package dev.ckateptb.minecraft.jyraf.config.serializer.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class LocationSerializer implements TypeSerializer<Location> {
    @Override
    public Location deserialize(Type type, ConfigurationNode node) throws SerializationException {
        World world = node.node("world").get(World.class);
        double x = node.node("x").getDouble();
        double y = node.node("y").getDouble();
        double z = node.node("z").getDouble();
        Location location = new Location(world, x, y, z);
        if (node.hasChild("yaw")) {
            location.setYaw(node.node("yaw").getFloat());
        }
        if (node.hasChild("pitch")) {
            location.setYaw(node.node("pitch").getFloat());
        }
        return location;
    }

    @Override
    public void serialize(Type type, @Nullable Location location, ConfigurationNode node) throws SerializationException {
        if (location == null) return;
        node.node("world", location.getWorld());
        node.node("x", location.getX());
        node.node("y", location.getY());
        node.node("z", location.getZ());
        float yaw = location.getYaw();
        if (yaw > 0) node.node("yaw", yaw);
        float pitch = location.getPitch();
        if (pitch > 0) node.node("pitch", pitch);
    }
}
