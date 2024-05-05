package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketNoteBlock extends PacketBlock {
    protected PacketNoteBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void spawnColoredNote(Collection<Player> players) {
        this.playAction(0, 0, players);
    }
}
