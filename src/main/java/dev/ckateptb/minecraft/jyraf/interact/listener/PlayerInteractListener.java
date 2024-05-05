package dev.ckateptb.minecraft.jyraf.interact.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.interact.PlayerInteractService;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.interact.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.Set;

@Component
public class PlayerInteractListener extends PacketListenerAbstract {
    private final PlayerInteractService service;

    private final WorldRepositoryService repository;
    private final PacketFactory factory;

    public PlayerInteractListener(PlayerInteractService service, WorldRepositoryService repository, PacketFactory factory) {
        super(PacketListenerPriority.HIGHEST);
        this.service = service;
        this.repository = repository;
        this.factory = factory;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            this.findBlock(player, wrapper.getBlockPosition())
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Pair.create(null, null))))
                    .subscribe(pair -> {
                        Block block = pair.getFirst();
                        PacketBlock packet = pair.getSecond();
                        this.service.tryFireInteract(player, MouseButton.RIGHT, block, null, packet, null);
                        // PacketBlockService - START
                        if (packet != null) {
                            event.setCancelled(true);
                        }
                        // PacketBlockService - END
                    });
        } else if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            if (player.getGameMode() == GameMode.ADVENTURE) return;
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            this.findBlock(player, wrapper.getBlockPosition())
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Pair.create(null, null))))
                    .subscribe(pair -> {
                        PacketBlock packet = pair.getSecond();
                        if (wrapper.getAction() == DiggingAction.START_DIGGING) {
                            Block block = pair.getFirst();
                            this.service.tryFireInteract(player, MouseButton.LEFT, block, null, packet, null);
                        }
                        // PacketBlockService - START
                        if (packet != null) {
                            event.setCancelled(true);
                            if(packet.isViewed(player)) {
                                this.factory.acknowledgeBlockChanges(player, wrapper.getSequence());
                            }
                        }
                        // PacketBlockService - END
                    });
        } else if (packetType == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            this.findBlock(player, null)
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Pair.create(null, null))))
                    .subscribe(pair -> {
                        Block block = pair.getFirst();
                        PacketBlock packet = pair.getSecond();
                        this.service.tryFireInteract(player, MouseButton.LEFT, block, null, packet, null);
                    });
        } else if (packetType == PacketType.Play.Client.USE_ITEM) {
            this.findBlock(player, null)
                    .switchIfEmpty(Mono.defer(() -> Mono.just(Pair.create(null, null))))
                    .subscribe(pair -> {
                        Block block = pair.getFirst();
                        PacketBlock packet = pair.getSecond();
                        this.service.tryFireInteract(player, MouseButton.RIGHT, block, null, packet, null);
                    });
        } else if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
            WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();
            if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;
            MouseButton button = action == WrapperPlayClientInteractEntity.InteractAction.INTERACT ?
                    MouseButton.RIGHT : MouseButton.LEFT;
            this.findEntity(player, wrapper.getEntityId())
                    .subscribe(entity -> {
                        Entity real = null;
                        PacketEntity packet = null;
                        if (entity.getT2()) {
                            packet = (PacketEntity) entity.getT1();
                        } else {
                            real = (Entity) entity.getT1();
                        }
                        this.service.tryFireInteract(player, button, null, real, null, packet);
                    });
        }
    }

    private Mono<PacketBlock> findPacketBlock(Player player, Vector3i position) {
        World world = player.getWorld();
        Location location = new Location(world, position.x, position.y, position.z);
        long chunkKey = Chunk.getChunkKey(location);
        return this.repository.getRepository(PacketBlock.class, world)
                .filterWhen(repository -> repository.hasChunk(chunkKey))
                .flatMap(repository -> repository.getChunk(chunkKey))
                .flatMapMany(Repository::get)
                .filter(block -> block.getVector3i().equals(position) && block.isViewed(player))
                .next();
    }

    private Mono<Pair<Block, PacketBlock>> findBlock(Player player, @Nullable Vector3i position) {
        World world = player.getWorld();
        Location center = player.getEyeLocation();
        Vector direction = center.getDirection().normalize();
        BlockIterator it = new BlockIterator(world, center.toVector(), direction, 0, 5);
        Set<Block> blocks = new HashSet<>();
        it.forEachRemaining(blocks::add);
        Location location = player.getLocation();
        return Flux.fromIterable(blocks)
                .filter(block -> position == null || new Vector3i(block.getX(), block.getY(), block.getZ()).equals(position))
                .flatMap(block -> this.findPacketBlock(player, new Vector3i(block.getX(), block.getY(), block.getZ()))
                        .map(packetBlock -> Pair.create(block, packetBlock))
                        .switchIfEmpty(Mono.defer(() -> Mono.just(Pair.create(block, null)))))
                .filter(pair -> pair.getSecond() != null || pair.getFirst().isSolid())
                .sort((o1, o2) -> {
                    Location first = o1.getFirst().getLocation();
                    Location second = o2.getFirst().getLocation();
                    return Double.compare(first.distanceSquared(location), second.distanceSquared(location));
                })
                .map(pair -> {
                    Block block = pair.getFirst();
                    return Pair.create(block.isSolid() ? block : null, pair.getSecond());
                })
                .next();
    }

    private Mono<Tuple2<?, Boolean>> findEntity(Player player, int entityId) {
        Location location = player.getLocation();
        World world = player.getWorld();
        return Flux.concat(
                        this.repository.getRepository(Entity.class, world),
                        this.repository.getRepository(PacketEntity.class, world)
                )
                .flatMap(repository -> repository.getNearbyChunks(location, 6.0D, 6.0D))
                .flatMap(Repository::get)
                .mapNotNull(object -> {
                    if (object instanceof PacketEntity entity && entity.getId() == entityId && entity.canView(player)) {
                        return Tuples.of(entity, true);
                    }
                    if (object instanceof Entity entity && entity.getEntityId() == entityId) {
                        return Tuples.of(entity, false);
                    }
                    return null;
                })
                .next();
    }
}
