package dev.ckateptb.minecraft.jyraf.packet.basic.interactable;

import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface IInteractable {

    @Nullable
    Location getLocation();

    @Nullable
    World getWorld();

    void handleInput(Player player, MouseButton type);

    @FunctionalInterface
    interface InteractionHandler {
        void handle(Player player, MouseButton type);
    }

}
