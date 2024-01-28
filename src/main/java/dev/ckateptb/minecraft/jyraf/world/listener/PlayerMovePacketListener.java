package dev.ckateptb.minecraft.jyraf.world.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class PlayerMovePacketListener extends PacketListenerAbstract {
    private final WorldService worldService;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        AtomicReference<Tuple2<Player, Vector3d>> playerReference = new AtomicReference<>();

        if (packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerPositionAndRotation wrapper = new WrapperPlayClientPlayerPositionAndRotation(event);
            playerReference.set(Tuples.of((Player) event.getPlayer(), wrapper.getPosition()));
        } else if (packetType == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition wrapper = new WrapperPlayClientPlayerPosition(event);
            playerReference.set(Tuples.of((Player) event.getPlayer(), wrapper.getPosition()));
        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            com.github.retrooper.packetevents.protocol.world.Location location = wrapper.getLocation();
            playerReference.set(Tuples.of((Player) event.getPlayer(), new Vector3d(location.getX(), location.getY(), location.getZ())));
        }

        Tuple2<Player, Vector3d> tuple = playerReference.get();
        if (tuple != null) {
            Player player = tuple.getT1();
            Vector3d vector3d = tuple.getT2();
            World world = player.getWorld();
            Location location = new Location(world, vector3d.x, vector3d.y, vector3d.z);
            this.worldService.getEntityById(world, player.getEntityId())
                    .filter(Objects::nonNull)
                    .subscribe(entity -> this.worldService.storeOrUpdateWithNewChunk(entity, Chunk.getChunkKey(location)));
        }
    }
}
