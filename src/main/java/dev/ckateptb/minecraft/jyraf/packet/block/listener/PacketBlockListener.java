package dev.ckateptb.minecraft.jyraf.packet.block.listener;

import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.repository.Repository;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PacketBlockListener implements Listener {
    private final WorldRepositoryService service;

    /*
      Если сервис отправит игроку блок до завершения chunk data этого блока, то игрок его не увидит.
        Чтобы исправить эту визуальную ошибку мы просто отправим блок повторно чуточку позже
    */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Flux.defer(() -> this.service.getRepository(PacketBlock.class, player.getWorld())
                        .flatMapMany(Repository::get)
                        .filter(block -> block.isViewed(player))
                        .delaySubscription(Duration.ofSeconds(3)))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(block -> block.spawn(List.of(player)));
    }
}
