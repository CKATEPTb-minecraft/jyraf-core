package dev.ckateptb.minecraft.jyraf.packet.block;

import com.github.retrooper.packetevents.util.Vector3i;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.enums.ClickType;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PacketBlock {

    private final Set<Player> allowedViewers = Collections.synchronizedSet(new HashSet<>());
    private final Set<Player> currentViewers = Collections.synchronizedSet(new HashSet<>());
    @Getter
    @Setter
    private boolean global = true;
    @Getter
    protected BlockData data;
    @Getter
    private final World world;
    @Getter
    private final Vector3i position;
    @Setter
    @Getter
    private PacketBlockInteractHandler interactHandler = null;

    public PacketBlock(Location location, BlockData data) {
        this.data = data.clone();
        this.world = location.getWorld();
        this.position = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

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
                        return (int)(first.distanceSquared(location) - second.distanceSquared(location));
                    });
                if (!this.global) flux = flux.filter(this.allowedViewers::contains);
                flux.collectList()
                    .doOnNext(players -> {
                        this.currentViewers.removeIf(player -> {
                            if (players.contains(player) && player.isOnline()) return false;
                            this.breakBlock(player);
                            return true;
                        });
                        players.forEach(player -> {
                            if (this.currentViewers.add(player)) {
                                this.placeBlock(player);
                            }
                        });
                    })
                    .subscribe();
            });
    }

    // todo: make instead of actionId enum
    //       params as well should be only pair depending on action
    public void playAction(int actionId) {
        this.playAction(actionId, 0);
    }

    public void playAction(int actionId, int param) {
        // todo: callback on it when player joins to area
        //       and if player broke block (i.e. chest), then, it should still exists
        this.currentViewers.forEach(viewer -> playAction(viewer, actionId, param));
    }

    public void playAction(Player player, int actionId) {
        this.playAction(player, actionId);
    }

    public void playAction(Player player, int actionId, int param) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.playBlockAction(player, this, actionId, param));
    }

    public void show() {
        this.global = true;
    }

    public void hide() {
        this.global = false;
        this.remove();
    }

    public boolean show(Player player) {
        return this.allowedViewers.add(player);
    }

    public boolean hide(Player player) {
        return this.allowedViewers.remove(player);
    }

    private void placeBlock(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.placeBlock(player, this));
    }

    private void breakBlock(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.breakBlock(player, this));
    }

    public void setData(BlockData data) {
        this.data = data;
        update();
    }

    public void update() {
        this.currentViewers.forEach(this::update);
    }

    public void update(Player player) {
        if (!this.currentViewers.contains(player)) return;
        this.placeBlock(player);
    }

    public boolean canView(Player player) {
        return this.currentViewers.contains(player);
    }

    public Location getLocation() {
        return new Location(world, this.position.x, this.position.y, this.position.z);
    }

    public void remove() {
        this.currentViewers.forEach(this::breakBlock);
    }

    public interface PacketBlockInteractHandler {
        void handle(Player player, ClickType clickType);
    }
}