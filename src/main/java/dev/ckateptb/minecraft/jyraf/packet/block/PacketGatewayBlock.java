package dev.ckateptb.minecraft.jyraf.packet.block;

import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketGatewayBlock extends PacketBlock {
    protected PacketGatewayBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void triggerBeam(Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.playBlockAction(player, this, 1, 0);
            }
        });
    }
}
