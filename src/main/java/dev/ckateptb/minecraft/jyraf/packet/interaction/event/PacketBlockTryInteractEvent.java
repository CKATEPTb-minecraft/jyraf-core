package dev.ckateptb.minecraft.jyraf.packet.interaction.event;

import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PacketBlockTryInteractEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PacketBlock block;
    private final MouseButton button;

    public PacketBlockTryInteractEvent(Player player, PacketBlock block, MouseButton button) {
        super(player, true);
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