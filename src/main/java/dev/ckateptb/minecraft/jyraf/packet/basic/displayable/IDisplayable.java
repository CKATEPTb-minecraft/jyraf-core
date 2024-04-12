package dev.ckateptb.minecraft.jyraf.packet.basic.displayable;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

public interface IDisplayable {

    void tick();

    @NotNull
    Flux<Player> getAllowedViewers();

    @NotNull
    Flux<Player> getCurrentViewers();

    void destroy(Player player);

    void display(Player player);

    boolean show(Player player);

    boolean hide(Player player);

    boolean canView(Player player);

    boolean isViewed(Player player);

    default void remove() {
        this.getCurrentViewers().subscribe(this::destroy);
    }

}