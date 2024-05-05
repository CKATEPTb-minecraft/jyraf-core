package dev.ckateptb.minecraft.jyraf.packet.block;

import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketContainerBlock extends PacketBlock {
    protected PacketContainerBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void open(Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.playBlockAction(player, this, 1, 1);
            }
        });
    }

    public void close(Collection<Player> players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.playBlockAction(player, this, 1, 0);
            }
        });
    }
}
