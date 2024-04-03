package dev.ckateptb.minecraft.jyraf.packet.interaction.event;

import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class PacketInteractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private long lastClicked = System.currentTimeMillis();
    private final @Nullable PacketBlock block;
    private final @Nullable PacketEntity entity;
    private final @NotNull MouseButton button;

    public PacketInteractEvent(@NotNull Player player, @Nullable PacketBlock block, @Nullable PacketEntity entity, @NotNull MouseButton button) {
        super(true);
        this.player = player;
        this.block = block;
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