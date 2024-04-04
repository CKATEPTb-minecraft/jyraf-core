package dev.ckateptb.minecraft.jyraf.packet.trait.implementation;

import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;

public class DisplayableTrait<T extends Displayable<T>> extends PacketTrait<T> {

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
    public void tick(@NotNull Mono<List<Player>> playersMono, @NotNull T displayable) {
        playersMono.doOnNext(players -> {
            displayable.getOriginalCurrentViewers().removeIf(player -> {
                if (players.contains(player) && player.isOnline()) return false;
                displayable.destroy(player);
                return true;
            });
            players.forEach(player -> {
                if (displayable.getOriginalCurrentViewers().add(player)) {
                    displayable.display(player);
                }
            });
        }).subscribe();
    }

}