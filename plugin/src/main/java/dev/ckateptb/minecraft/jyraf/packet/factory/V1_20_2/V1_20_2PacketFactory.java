package dev.ckateptb.minecraft.jyraf.packet.factory.V1_20_2;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.packet.factory.V1_19_3.V1_19_3PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

public class V1_20_2PacketFactory extends V1_19_3PacketFactory {
    @Override
    public void spawnPlayer(Player player, PacketEntity entity) {
        Mono.defer(() -> {
                    this.addTabPlayer(player, entity);
                    return Mono.just(true);
                })
                .doFinally((signalType) -> Bukkit.getScheduler().runTaskLaterAsynchronously(Jyraf.getPlugin(),
                        () -> this.removeTabPlayer(player, entity), 60))
                .subscribe(ignored -> {
                    this.spawnEntity(player, entity);
                    Location location = entity.getLocation();
                    this.rotate(player, entity, location.getYaw(), location.getPitch());
                    this.sendMetadata(player, entity);
                    this.createTeam(player, entity);
                });
    }
}
