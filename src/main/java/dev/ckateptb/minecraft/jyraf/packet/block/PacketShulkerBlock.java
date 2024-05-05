package dev.ckateptb.minecraft.jyraf.packet.block;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class PacketShulkerBlock extends PacketContainerBlock {
    protected PacketShulkerBlock(Location location, BlockData data) {
        super(location, data);
    }
}
