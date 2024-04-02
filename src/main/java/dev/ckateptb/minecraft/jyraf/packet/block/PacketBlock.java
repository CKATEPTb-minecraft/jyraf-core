package dev.ckateptb.minecraft.jyraf.packet.block;

import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import dev.ckateptb.minecraft.jyraf.colider.Colliders;
import dev.ckateptb.minecraft.jyraf.packet.basic.Interactable;
import dev.ckateptb.minecraft.jyraf.packet.enums.BlockAction;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class PacketBlock extends Interactable {

    protected BlockData data;
    private final World world;
    private final Vector3i position;

    public PacketBlock(@NotNull Location location, BlockData data) {
        this(location, data, true);
    }

    public PacketBlock(@NotNull Location location, BlockData data, boolean global) {
        this(location, data, new ArrayList<>());
        this.global = global;
    }

    public PacketBlock(@NotNull Location location, BlockData data, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        this.data = data.clone();
        this.world = location.getWorld();
        this.position = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.location = location;
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
                    // todo: move it to trait i think
                    mono.doOnNext(players -> {
                                this.currentViewers.removeIf(player -> {
                                    if (players.contains(player) && player.isOnline()) return false;
                                    this.destroy(player);
                                    return true;
                                });
                                players.forEach(player -> {
                                    if (this.currentViewers.add(player)) {
                                        this.display(player);
                                    }
                                });
                            })
                            .subscribe();
//                    for (PacketTrait<?> unknownTrait : this.getTraits()) {
//                        if (unknownTrait.getEntryClass() != PacketBlock.class) continue;
//                        PacketTrait<PacketBlock> trait = (PacketTrait<PacketBlock>) unknownTrait;
//                        if (trait.isCancelled()) continue;
//                        mono.doOnNext(players -> players.forEach(player -> trait.tick(player, this))).subscribe();
//                    }
                });
    }

    public void playAction(BlockAction action) {
        this.allowedViewers.forEach(player -> this.playAction(player, action));
    }

    public void playAction(Player player, BlockAction action) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.playBlockAction(player, this, action));
    }

    private void display(Player player, WrapperPlayClientPlayerDigging wrapper) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> {
            factory.placeBlock(player, this);
            if (wrapper == null) return;
            factory.acknowledgeBlockChanges(player, wrapper.getSequence());
        });
    }

    public void setData(BlockData data) {
        this.data = data;
        update();
    }

    public void update() {
        this.currentViewers.forEach(this::update);
    }

    public void update(Player player) {
        this.update(player, null);
    }

    public void update(Player player, WrapperPlayClientPlayerDigging wrapper) {
        if (!this.currentViewers.contains(player)) return;
        this.display(player, wrapper);
    }

    @Override
    public void display(Player player) {
        this.display(player, null);
        player.sendMessage("shown to u block!");
    }

    @Override
    public void destroy(Player player) {
        PacketFactory.INSTANCE.get().ifPresent(factory -> factory.breakBlock(player, this));
        player.sendMessage("hidden block from u");
    }

    @Override
    public Location getLocation() {
        return new Location(world, this.position.x, this.position.y, this.position.z);
    }

}