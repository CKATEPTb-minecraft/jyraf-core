package dev.ckateptb.minecraft.jyraf.packet.bossbar;

import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;
import dev.ckateptb.minecraft.jyraf.packet.trait.implementation.DisplayableTrait;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketBossBar extends Displayable {

    public PacketBossBar(@NotNull Location location) {
        this(location, true);
    }

    public PacketBossBar(@NotNull Location location, boolean global) {
        this(location, new ArrayList<>());
        this.global = global;
    }

    public PacketBossBar(@NotNull Location location, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        addTrait(new DisplayableTrait<>(PacketBossBar.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tick() {
        Location location = this.getLocation();
        Colliders.sphere(location, 20)
                .affectEntities(entities -> {
                    Flux<Player> flux = entities
                            .filter(entity -> entity instanceof Player)
                            .cast(Player.class)
                            .sort((o1, o2) -> {
                                Location first = o1.getLocation();
                                Location second = o2.getLocation();
                                return (int) (first.distanceSquared(location) - second.distanceSquared(location));
                            });
                    if (!this.global) flux = flux.filter(this.allowedViewers::contains);
                    Mono<List<Player>> mono = flux.collectList();
                    // todo: cache traits in needed order,
                    //       make traits ticking inside of some of parent classes
                    for (PacketTrait<?> unknownTrait : this.getTraits()) {
                        if (unknownTrait.getEntryClass() != PacketBossBar.class) continue;
                        PacketTrait<PacketBossBar> trait = (PacketTrait<PacketBossBar>) unknownTrait;
                        if (trait.isCancelled()) continue;
                        mono.doOnNext(players ->
                                players.forEach(player ->
                                        trait.tick(player, this))).subscribe();
                    }
                });
    }

    @Override
    public void destroy(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.hideBossBar(player, this));
    }

    @Override
    public void display(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.showBossBar(player, this));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Color {
        PINK(0),
        BLUE(1),
        RED(2),
        GREEN(3),
        YELLOW(4),
        PURPLE(5),
        WHITE(6);

        private final int id;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Style {
        SOLID(0),
        NOTCHED_6(1),
        NOTCHED_10(2),
        NOTCHED_12(3),
        NOTCHED_20(4);

        private final int id;
    }

}