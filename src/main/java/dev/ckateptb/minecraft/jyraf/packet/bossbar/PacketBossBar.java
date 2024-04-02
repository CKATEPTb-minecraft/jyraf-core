package dev.ckateptb.minecraft.jyraf.packet.bossbar;

import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PacketBossBar extends Displayable {

    public PacketBossBar(@NotNull Location location) {
        super(location);
    }

    public PacketBossBar(@NotNull Location location, boolean global) {
        super(location, global);
    }

    public PacketBossBar(@NotNull Location location, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
    }

    @Override
    public void tick() {
        // todo
    }

    @Override
    public void destroy(Player player) {
        // todo
        throw new NotImplementedException();
    }

    @Override
    public void display(Player player) {
        // todo
        throw new NotImplementedException();
    }

}