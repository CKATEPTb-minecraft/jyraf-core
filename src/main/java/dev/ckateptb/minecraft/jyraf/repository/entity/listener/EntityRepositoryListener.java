package dev.ckateptb.minecraft.jyraf.repository.entity.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.repository.WorldRepositoryService;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class EntityRepositoryListener implements Listener {
    private final Jyraf plugin;
    private final WorldRepositoryService service;

    @EventHandler
    public void on(EntityAddToWorldEvent event) {
        this.handleEntity(event.getEntity(), true);
    }

    @EventHandler
    public void on(EntityRemoveFromWorldEvent event) {
        this.handleEntity(event.getEntity(), false);
    }

    private void handleEntity(Entity entity, boolean add) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Mono.defer(() -> EntityRepositoryListener.this.service.getRepository(Entity.class, entity.getWorld()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(repository -> add ? repository.add(entity) : repository.remove(entity))
                        .subscribe();
            }
        };
        if (entity instanceof Player && add) {
            runnable.runTaskLater(this.plugin, 20);
        } else runnable.run();
    }
}
