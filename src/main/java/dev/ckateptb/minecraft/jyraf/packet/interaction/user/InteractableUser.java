package dev.ckateptb.minecraft.jyraf.packet.interaction.user;

import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InteractableUser {

    @NotNull
    private final UUID uniqueId;
    @NotNull
    private final Player player;
    private long lastInteractionTime;
    @Nullable
    private MouseButton lastInteractedButton;

    public InteractableUser(@NotNull UUID uniqueId, @NotNull Player player, @NotNull MouseButton button) {
        this(uniqueId, player, button, System.currentTimeMillis());
    }

    public InteractableUser(@NotNull UUID uniqueId, @NotNull Player player, @NotNull MouseButton button, long time) {
        Objects.requireNonNull(uniqueId);
        Objects.requireNonNull(player);
        Objects.requireNonNull(button);
        this.uniqueId = uniqueId;
        this.player = player;
        this.lastInteractionTime = time;
        this.lastInteractedButton = button;
    }

}