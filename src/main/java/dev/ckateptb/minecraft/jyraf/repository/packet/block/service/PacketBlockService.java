package dev.ckateptb.minecraft.jyraf.repository.packet.block.service;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.ClickType;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import dev.ckateptb.minecraft.jyraf.repository.packet.block.PacketBlockRepository;
import dev.ckateptb.minecraft.jyraf.repository.world.chunk.AbstractChunkRepository;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

@Component
public class PacketBlockService extends PacketListenerAbstract {

    private final WorldRepositoryService service;

    public PacketBlockService(WorldRepositoryService service) {
        super(PacketListenerPriority.HIGHEST);
        this.service = service;
    }

    private void handleBlockInteract(Player player, PacketBlock block, boolean rightClick) {
        block.getInteractHandler()
            .handle(player, rightClick ? ClickType.RIGHT : ClickType.LEFT);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) { // LMB
            if (!(event.getPlayer() instanceof Player player)) return;
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(packetBlock -> {
                if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                    this.handleBlockInteract(player, packetBlock, false);
                }
                event.setCancelled(true);
                packetBlock.update(player);
            });
        }
        else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) { // RMB
            if (!(event.getPlayer() instanceof Player player)) return;
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(packetBlock -> {
                this.handleBlockInteract(player, packetBlock, true);
            });
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.BLOCK_CHANGE) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
        this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(block -> {
            wrapper.setBlockState(SpigotConversionUtil.fromBukkitBlockData(block.getData()));
        });
    }

    private Mono<PacketBlock> findBlock(Player player, World world, Vector3i position) {
        Location location = new Location(world, position.x, position.y, position.z);
        return this.service.getRepository(PacketBlock.class, world)
            .cast(PacketBlockRepository.class)
            .flatMap(blockRepository -> blockRepository.getChunk(Chunk.getChunkKey(location)))
            .cast(PacketBlockRepository.PacketBlockChunkRepository.class)
            .flatMapMany(AbstractChunkRepository::get)
            .cast(PacketBlock.class)
            .filter(block -> block.getPosition().equals(position) && block.isViewed(player))
            .next();
    }
}