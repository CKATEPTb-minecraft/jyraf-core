package dev.ckateptb.minecraft.jyraf.packet.factory.V1_17;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.factory.v1_8.V1_8PacketFactory;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class V1_17PacketFactory extends V1_8PacketFactory {
    @Override
    public void spawnEntity(Player player, PacketEntity entity) {
        Location location = entity.getLocation();
        this.sendPacket(player, new WrapperPlayServerSpawnEntity(entity.getId(), entity.getUniqueId(),
                SpigotConversionUtil.fromBukkitEntityType(entity.getType()),
                SpigotConversionUtil.fromBukkitLocation(location),
                location.getYaw(), 0, new Vector3d()
        ));
        this.sendMetadata(player, entity);
        this.createTeam(player, entity);
    }
}
