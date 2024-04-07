package dev.ckateptb.minecraft.jyraf.packet.interaction;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketInteractEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class InteractionService implements Listener {

    private final AsyncCache<UUID, Long> rmbCooldowns = Caffeine.newBuilder()
            .buildAsync();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PacketInteractEvent event) {
        Player player = event.getPlayer();
        MouseButton button = event.getButton();
        rmbCooldowns.get(event.getPlayer().getUniqueId(), (uuid) -> System.currentTimeMillis()).thenAcceptAsync((time) -> {
            if (button == MouseButton.LEFT && player.getGameMode() == GameMode.ADVENTURE && System.currentTimeMillis() < time + 500)
                return;
            rmbCooldowns.put(event.getPlayer().getUniqueId(), CompletableFuture.completedFuture(System.currentTimeMillis() + 500));
            if (event.getBlock() != null) event.getBlock().handleInput(player, event.getButton());
            if (event.getEntity() != null) event.getEntity().handleInput(player, event.getButton());
        });
    }

}