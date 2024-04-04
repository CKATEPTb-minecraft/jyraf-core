package dev.ckateptb.minecraft.jyraf.packet.trait.implementation;

import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;

public class DisplayableTrait extends PacketTrait<Displayable> {

    private final Class<Displayable> clazz;

    public DisplayableTrait(Class<Displayable> clazz) {
        super(EventPriority.MONITOR);
        this.clazz = clazz;
    }

    @Override
    public @NotNull Class<Displayable> getEntryClass() {
        return clazz;
    }

    @Override
    public void tick(@NotNull Mono<List<Player>> playersMono, @NotNull Displayable displayable) {
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