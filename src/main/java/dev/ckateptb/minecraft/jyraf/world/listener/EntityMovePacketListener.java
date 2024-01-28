package dev.ckateptb.minecraft.jyraf.world.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class EntityMovePacketListener extends PacketListenerAbstract {
    private final WorldService worldService;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PacketTypeCommon packetType = event.getPacketType();
        AtomicInteger entityId = new AtomicInteger(Integer.MIN_VALUE);
        if (packetType == PacketType.Play.Server.ENTITY_MOVEMENT) {
            WrapperPlayServerEntityMovement packet = new WrapperPlayServerEntityMovement(event);
            entityId.set(packet.getEntityId());
        } else if (packetType == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event);
            entityId.set(packet.getEntityId());
        } else if (packetType == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            entityId.set(packet.getEntityId());
        } else if (packetType == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(event);
            entityId.set(packet.getEntityId());
        } else if (packetType == PacketType.Play.Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
            entityId.set(packet.getEntityId());
        }
        int id = entityId.get();
        if (id != Integer.MIN_VALUE) {
            World world = player.getWorld();
            this.worldService.getEntityById(world, id)
                    .filter(Objects::nonNull)
                    .subscribe(this.worldService::storeOrUpdate);
        }
    }
}
