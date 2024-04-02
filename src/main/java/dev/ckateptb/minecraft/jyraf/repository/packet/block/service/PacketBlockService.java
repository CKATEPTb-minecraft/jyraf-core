package dev.ckateptb.minecraft.jyraf.repository.packet.block.service;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// todo: make all-in-one service for Displayable, Interactable, e.t.c.
@Component
@RequiredArgsConstructor
public class PacketBlockService extends PacketListenerAbstract {

    private final WorldRepositoryService service;

    private void handleBlockInteract(Player player, PacketBlock block, boolean rightButton) {
        block.handleInput(player, MouseButton.right(rightButton));
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) { // LMB gm 2 todo fix handling when right click chest
            if (player.getGameMode() != GameMode.ADVENTURE) return;
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            RayTraceResult result = player.rayTraceBlocks(5.0);
            if (result == null) return;
            Block block = result.getHitBlock();
            if (block == null) return;
            this.findBlock(player, block.getWorld(), SpigotConversionUtil.fromBukkitLocation(block.getLocation()).getPosition().toVector3i())
                    .subscribe(packetBlock -> this.handleBlockInteract(player, packetBlock, false));
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) { // LMB for other gamemodes
            if (player.getGameMode() == GameMode.ADVENTURE) return;
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            this.findBlock(player, player.getWorld(), wrapper.getBlockPosition()).subscribe(packetBlock -> {
                if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                    this.handleBlockInteract(player, packetBlock, false);
                }
                event.setCancelled(true);
                packetBlock.update(player, wrapper);
            });
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) { // RMB
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
        if (type == PacketType.Play.Server.CHUNK_DATA) {
            WrapperPlayServerChunkData clone = new WrapperPlayServerChunkData(event.clone());
            Column cloneColumn = clone.getColumn();
            long chunkKey = Chunk.getChunkKey(cloneColumn.getX(), cloneColumn.getZ());
            this.service.getRepository(PacketBlock.class, world)
                    .filterWhen(repository -> repository.hasChunk(chunkKey))
                    .flatMap(repository -> repository.getChunk(chunkKey))
                    .flatMapMany(repository -> {
                        CachedReference<BaseChunk[]> cache = new CachedReference<>(() ->
                                new WrapperPlayServerChunkData(event).getColumn().getChunks());
                        return repository.get()
                                .filter(block -> block.isViewed(player))
                                .doOnNext(block -> {
                                    Vector3i position = block.getPosition();
                                    int x = position.getX() & 15;
                                    int y = position.getY() & 15;
                                    int z = position.getZ() & 15;
                                    WrappedBlockState state = SpigotConversionUtil.fromBukkitBlockData(block.getData());
                                    cache.get().ifPresent(chunks -> {
                                        for (BaseChunk chunk : chunks) {
                                            if (chunk == null) continue;
                                            chunk.set(x, y, z, state);
                                        }
                                    });
                                });
                    })
                    .subscribe();
        } else if (type == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
            Flux.fromArray(wrapper.getBlocks())
                    .flatMap(origin -> {
                        Vector3i position = new Vector3i(origin.getX(), origin.getY(), origin.getZ());
                        return this.findBlock(player, world, position)
                                .doOnNext(block -> origin.setBlockState(SpigotConversionUtil.fromBukkitBlockData(block.getData())));
                    })
                    .subscribe();
        } else if (type == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            this.findBlock(player, world, wrapper.getBlockPosition()).subscribe(block ->
                    wrapper.setBlockState(SpigotConversionUtil.fromBukkitBlockData(block.getData())));
        }
    }

    private Mono<PacketBlock> findBlock(Player player, World world, Vector3i position) {
        Location location = new Location(world, position.x, position.y, position.z);
        long chunkKey = Chunk.getChunkKey(location);
        return this.service.getRepository(PacketBlock.class, world)
                .filterWhen(repository -> repository.hasChunk(chunkKey))
                .flatMap(repository -> repository.getChunk(chunkKey))
                .flatMapMany(Repository::get)
                .filter(block -> block.getPosition().equals(position) && block.isViewed(player))
                .next();
    }
}