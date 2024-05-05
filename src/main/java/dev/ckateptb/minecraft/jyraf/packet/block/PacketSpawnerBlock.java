package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketSpawnerBlock extends PacketBlock {
    protected PacketSpawnerBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void resetDelay(Collection<Player> players) {
        this.playAction(1, 0, players);
    }
}
