package dev.ckateptb.minecraft.jyraf.packet.bossbar;

import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

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