package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class PacketChestBlock extends PacketContainerBlock {
    protected PacketChestBlock(Location location, BlockData data) {
        super(location, data);
    }
}
