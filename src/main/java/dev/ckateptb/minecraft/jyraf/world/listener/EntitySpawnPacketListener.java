package dev.ckateptb.minecraft.jyraf.world.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EntitySpawnPacketListener extends PacketListenerAbstract {
    private final WorldService worldService;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity entityPacket = new WrapperPlayServerSpawnEntity(event);
            int entityId = entityPacket.getEntityId();
            World world = player.getWorld();
            this.worldService.getEntityById(world, entityId)
                    .filter(Objects::nonNull)
                    .subscribe(this.worldService::storeOrUpdate);
        }
    }
}
