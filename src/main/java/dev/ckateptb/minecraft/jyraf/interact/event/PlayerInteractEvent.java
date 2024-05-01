package dev.ckateptb.minecraft.jyraf.interact.event;

import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class PlayerInteractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final MouseButton button;
    private final Block clickedBlock;
    private final Entity clickedEntity;
    private final PacketBlock clickedPacketBlock;
    private final PacketEntity clickedPacketEntity;

    public PlayerInteractEvent(Player player, MouseButton button, Block block, Entity entity,
                               PacketBlock clickedPacketBlock, PacketEntity clickedPacketEntity) {
        super(true);
        this.player = player;
        this.button = button;
        this.clickedBlock = block;
        this.clickedEntity = entity;
        this.clickedPacketBlock = clickedPacketBlock;
        this.clickedPacketEntity = clickedPacketEntity;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Optional<Block> getClickedBlock() {
        return Optional.ofNullable(this.clickedBlock);
    }

    public Optional<Entity> getClickedEntity() {
        return Optional.ofNullable(this.clickedEntity);
    }

    public Optional<PacketBlock> getClickedPacketBlock() {
        return Optional.ofNullable(this.clickedPacketBlock);
    }

    public Optional<PacketEntity> getClickedPacketEntity() {
        return Optional.ofNullable(this.clickedPacketEntity);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
