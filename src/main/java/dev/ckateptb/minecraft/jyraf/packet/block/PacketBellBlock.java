package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketBellBlock extends PacketBlock {
    protected PacketBellBlock(Location location, BlockData data) {
        super(location, data);
    }

    public void ring(BlockFace facing, Collection<Player> players) {
        this.playAction(1, this.getFacingParam(facing), players);
    }

    private int getFacingParam(BlockFace facing) {
        return switch (facing) {
            case DOWN -> 0;
            case UP -> 1;
            case SOUTH -> 3;
            case WEST -> 4;
            case NORTH -> 2;
            case EAST -> 5;
            default -> throw new IllegalStateException("Unsupported facing: " + facing);
        };
    }
}
