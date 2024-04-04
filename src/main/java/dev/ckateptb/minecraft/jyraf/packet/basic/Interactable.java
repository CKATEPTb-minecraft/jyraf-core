package dev.ckateptb.minecraft.jyraf.packet.basic;

import dev.ckateptb.minecraft.jyraf.packet.basic.interactable.IInteractable;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Setter
@Getter
public abstract class Interactable<T extends Displayable<T>> extends Displayable<T> implements IInteractable {

    @Nullable
    protected InteractionHandler interactionHandler = (player, button) -> {

    };

    public Interactable(@NotNull Location location) {
        super(location);
    }

    public Interactable(@NotNull Location location, boolean global) {
        super(location, global);
    }

    public Interactable(@NotNull Location location, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
    }

    @Override
    @NotNull
    public World getWorld() {
        return this.location.getWorld();
    }

    @Override
    public void handleInput(Player player, MouseButton type) {
        if (this.interactionHandler == null) return;
        this.interactionHandler.handle(player, type);
    }

}