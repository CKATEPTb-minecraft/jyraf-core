package dev.ckateptb.minecraft.jyraf.packet.interaction.event;

import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import dev.ckateptb.minecraft.jyraf.packet.interaction.user.InteractableUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PacketBlockInteractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final InteractableUser user;
    private final PacketBlock block;
    private final MouseButton button;

    public PacketBlockInteractEvent(InteractableUser user, PacketBlock block, MouseButton button) {
        super(true);
        this.user = user;
        this.block = block;
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