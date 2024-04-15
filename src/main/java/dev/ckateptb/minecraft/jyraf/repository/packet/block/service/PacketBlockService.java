package dev.ckateptb.minecraft.jyraf.repository.packet.block.service;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

// todo: make all-in-one service for Displayable, Interactable, e.t.c.
@Component
@RequiredArgsConstructor
public class PacketBlockService extends PacketListenerAbstract {

    private final WorldRepositoryService service;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PacketTypeCommon type = event.getPacketType();
        World world = player.getWorld();
        if (type == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            this.findBlock(player, world, wrapper.getBlockPosition()).subscribe(block ->
                    wrapper.setBlockState(SpigotConversionUtil.fromBukkitBlockData(block.getBukkitData())));
        }
    }

    private Mono<PacketBlock> findBlock(Player player, World world, Vector3i position) {
        Location location = new Location(world, position.x, position.y, position.z);
        long chunkKey = Chunk.getChunkKey(location);
        return this.service.getRepository(PacketBlock.class, world)
                .filterWhen(repository -> repository.hasChunk(chunkKey))
                .flatMap(repository -> repository.getChunk(chunkKey))
                .flatMapMany(Repository::get)
                .filter(block -> block.getVector().equals(position) && block.isViewed(player))
                .next();
    }
}