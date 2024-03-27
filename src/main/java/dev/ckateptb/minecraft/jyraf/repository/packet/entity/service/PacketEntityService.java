package dev.ckateptb.minecraft.jyraf.repository.packet.entity.service;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.ClickType;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import dev.ckateptb.minecraft.jyraf.repository.packet.entity.PacketEntityRepository;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

@Component
public class PacketEntityService extends PacketListenerAbstract {

    private final WorldRepositoryService service;

    public PacketEntityService(WorldRepositoryService service) {
        super(PacketListenerPriority.HIGHEST);
        this.service = service;
    }

    private void handleEntityInteract(Player player, PacketEntity entity, boolean rightClick) {
        PacketEntity.PacketEntityInteractHandler handler = entity.getInteractHandler();
        if (handler == null) return;
        handler.handle(player, rightClick ? ClickType.RIGHT : ClickType.LEFT);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getHand() != InteractionHand.MAIN_HAND) return;
        WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();
        if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;
        this.findEntity(player, player.getLocation(), wrapper.getEntityId()).subscribe(entity -> this
                .handleEntityInteract(player, entity, action == WrapperPlayClientInteractEntity.InteractAction.INTERACT));
    }

    private Mono<PacketEntity> findEntity(Player player, Location location, int id) {
        return this.service.getRepository(PacketEntity.class, location.getWorld())
                .cast(PacketEntityRepository.class)
                .flatMapMany(entityRepository -> entityRepository.getNearbyChunks(location, 6.0D, 6.0D))
                .flatMap(Repository::get)
                .cast(PacketEntity.class)
                .filter(entity -> entity.getId() == id && entity.canView(player))
                .next();
    }
}