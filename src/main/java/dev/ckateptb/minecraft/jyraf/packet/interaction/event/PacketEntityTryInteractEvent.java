package dev.ckateptb.minecraft.jyraf.packet.interaction.event;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PacketEntityTryInteractEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PacketEntity entity;
    private final MouseButton button;

    public PacketEntityTryInteractEvent(Player player, PacketEntity entity, MouseButton button) {
        super(player, true);
        this.entity = entity;
        this.button = button;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}