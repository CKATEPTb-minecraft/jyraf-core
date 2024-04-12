package dev.ckateptb.minecraft.jyraf.repository.packet.block.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketInteractEvent;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

// todo: make all-in-one service for Displayable, Interactable, e.t.c.
@Component
@RequiredArgsConstructor
public class PacketBlockService extends PacketListenerAbstract {

    private final WorldRepositoryService service;
    private final AsyncCache<Vector3i, PacketBlock> cachedBlocks = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .buildAsync();

    @Schedule(async = true, initialDelay = 40, fixedRate = 40)
    private void tick() {
        this.cachedBlocks.asMap().values().forEach(future -> future
                .thenAcceptAsync(block -> {
                    WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(block.getVector(), block.getData().getGlobalId());
                    wrapper.setBlockState(SpigotConversionUtil.fromBukkitBlockData(block.getBukkitData()));
                    block.getCurrentViewers()
                            .subscribe(player -> PacketFactory.INSTANCE.consume(factory -> factory.sendPacket(player, wrapper)));
                }));
    }

    private void handleBlockInteract(Player player, PacketBlock block, boolean rightButton) {
        MouseButton button = MouseButton.right(rightButton);
        new PacketInteractEvent(player, block, null, button).callEvent();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (player.getGameMode() != GameMode.ADVENTURE) return;
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            RayTraceResult result = player.rayTraceBlocks(5.0);
            if (result == null) return;
            Block block = result.getHitBlock();
            if (block == null) return;
            this.findBlock(player, block.getWorld(), SpigotConversionUtil.fromBukkitLocation(block.getLocation()).getPosition().toVector3i())
                    .subscribe(packetBlock -> this.handleBlockInteract(player, packetBlock, false));
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            if (player.getGameMode() == GameMode.ADVENTURE) return;
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(packetBlock -> {
                if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                    this.handleBlockInteract(player, packetBlock, false);
                }
                event.setCancelled(true);
                packetBlock.update(player, wrapper);
            });
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(packetBlock -> {
                this.handleBlockInteract(player, packetBlock, true);
                event.setCancelled(true);
            });
        }
    }

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
        return Mono.fromFuture(this.cachedBlocks.get(position, (vector) -> {
            Location location = new Location(world, position.x, position.y, position.z);
            long chunkKey = Chunk.getChunkKey(location);
            return this.service.getRepository(PacketBlock.class, world)
                    .filterWhen(repository -> repository.hasChunk(chunkKey))
                    .flatMap(repository -> repository.getChunk(chunkKey))
                    .flatMapMany(Repository::get)
                    .filter(block -> block.getVector().equals(position) && block.isViewed(player))
                    .next()
                    .block();
        }));
    }
}