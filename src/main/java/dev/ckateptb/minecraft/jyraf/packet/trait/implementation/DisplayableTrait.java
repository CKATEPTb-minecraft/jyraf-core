package dev.ckateptb.minecraft.jyraf.packet.trait.implementation;

import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class DisplayableTrait<T extends Displayable> extends PacketTrait<T> {

    private final Class<T> clazz;

    public DisplayableTrait(Class<T> clazz) {
        super(EventPriority.MONITOR);
        this.clazz = clazz;
    }

    @Override
    public @NotNull Class<T> getEntryClass() {
        return clazz;
    }

    @Override
    public void tick(@NotNull Player player, @NotNull Displayable entry) {
        // todo: fix this remove
        entry.getOriginalCurrentViewers().removeIf(targetPlayer -> {
            if (targetPlayer.isOnline() && player.equals(targetPlayer)) return false;
            entry.destroy(targetPlayer);
            return true;
        });
        if (entry.getOriginalCurrentViewers().add(player)) {
            entry.display(player);
        }
    }

}