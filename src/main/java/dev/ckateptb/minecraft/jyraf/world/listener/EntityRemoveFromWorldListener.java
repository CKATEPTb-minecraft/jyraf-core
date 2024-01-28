package dev.ckateptb.minecraft.jyraf.world.listener;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@Component
@RequiredArgsConstructor
public class EntityRemoveFromWorldListener implements Listener {
    WorldService worldService;

    @EventHandler
    public void on(@NotNull EntityRemoveFromWorldEvent event) {
        worldService.removeEntity(event.getEntity());
    }
}
