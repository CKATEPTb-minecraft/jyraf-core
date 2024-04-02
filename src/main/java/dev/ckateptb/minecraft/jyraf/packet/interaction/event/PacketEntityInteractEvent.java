package dev.ckateptb.minecraft.jyraf.packet.interaction.event;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.interaction.user.InteractableUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PacketEntityInteractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final InteractableUser user;
    private final PacketEntity entity;
    private final MouseButton button;

    public PacketEntityInteractEvent(InteractableUser user, PacketEntity entity, MouseButton button) {
        super(true);
        this.user = user;
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