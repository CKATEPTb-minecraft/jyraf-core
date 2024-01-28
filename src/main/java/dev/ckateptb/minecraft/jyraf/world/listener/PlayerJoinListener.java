package dev.ckateptb.minecraft.jyraf.world.listener;

import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.world.WorldService;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {
    private final WorldService worldService;

    @EventHandler
    public void on(@NotNull PlayerJoinEvent event) {
        this.worldService.getEntityById(event.getPlayer().getWorld(), event.getPlayer().getEntityId())
                .filter(Objects::nonNull)
                .subscribe(this.worldService::storeOrUpdate);
    }

}
