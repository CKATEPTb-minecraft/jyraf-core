package dev.ckateptb.minecraft.jyraf.packet.interaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketBlockInteractEvent;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketBlockTryInteractEvent;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketEntityInteractEvent;
import dev.ckateptb.minecraft.jyraf.packet.interaction.event.PacketEntityTryInteractEvent;
import dev.ckateptb.minecraft.jyraf.packet.interaction.user.InteractableUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

@Component
public class InteractionService implements Listener {

    private final Cache<UUID, InteractableUser> users = Caffeine.newBuilder()
            .build();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PacketBlockInteractEvent event) {
        event.getBlock().handleInput(event.getUser().getPlayer(), event.getButton());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PacketEntityInteractEvent event) {
        event.getEntity().handleInput(event.getUser().getPlayer(), event.getButton());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PacketBlockTryInteractEvent event) {
        Player player = event.getPlayer();
        MouseButton button = event.getButton();
        InteractableUser interactableUser = users.get(player.getUniqueId(), (uuid) -> new InteractableUser(uuid, player, button));
        if (button == MouseButton.LEFT && System.currentTimeMillis() < interactableUser.getLastInteractionTime() + 500)
            return;
        interactableUser.setLastInteractionTime(System.currentTimeMillis());
        new PacketBlockInteractEvent(interactableUser, event.getBlock(), button).callEvent();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PacketEntityTryInteractEvent event) {
        Player player = event.getPlayer();
        MouseButton button = event.getButton();
        InteractableUser interactableUser = users.get(player.getUniqueId(), (uuid) -> new InteractableUser(uuid, player, button));
        interactableUser.setLastInteractionTime(System.currentTimeMillis());
        new PacketEntityInteractEvent(interactableUser, event.getEntity(), button).callEvent();
    }

}