package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketContainerBlock extends PacketBlock {
    protected PacketContainerBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void open(Collection<Player> players) {
        this.playAction(1, 1, players);
    }

    public void close(Collection<Player> players) {
        this.playAction(1, 0, players);
    }
}
