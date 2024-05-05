package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketPistonBlock extends PacketBlock {
    protected PacketPistonBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void extend(Collection<Player> players) {
        this.playAction(0, this.getFacingParam(), players);
    }

    public void retract(Collection<Player> players) {
        this.playAction(1, this.getFacingParam(), players);
    }

    private int getFacingParam() {
        BlockFace facing = ((Piston) this.data).getFacing();
        return switch (facing) {
            case DOWN -> 0;
            case UP -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            case NORTH -> 4;
            case EAST -> 5;
            default -> throw new IllegalStateException("Unsupported facing: " + facing);
        };
    }
}
