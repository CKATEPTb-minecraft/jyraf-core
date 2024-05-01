package dev.ckateptb.minecraft.jyraf.packet.factory.V1_20_2;

import dev.ckateptb.minecraft.jyraf.packet.entity.PacketPlayer;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_19_3.V1_19_3PacketFactory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class V1_20_2PacketFactory extends V1_19_3PacketFactory {
    @Override
    public void spawnPlayer(@NotNull Player player, @NotNull PacketPlayer entity) {
        this.spawnEntity(player, entity);
    }
}
