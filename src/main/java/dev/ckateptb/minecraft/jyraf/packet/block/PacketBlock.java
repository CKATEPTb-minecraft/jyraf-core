package dev.ckateptb.minecraft.jyraf.packet.block;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import dev.ckateptb.minecraft.jyraf.packet.basic.Interactable;
import dev.ckateptb.minecraft.jyraf.packet.enums.BlockAction;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Getter
public class PacketBlock extends Interactable {

    protected WrappedBlockState data;
    private final World world;
    private final Vector3i vector;

    public PacketBlock(@NotNull Location location, @NotNull BlockData data) {
        this(location, data, true);
    }

    public PacketBlock(@NotNull Location location, @NotNull BlockData data, boolean global) {
        this(location, data, global, new ArrayList<>());
    }

    public PacketBlock(@NotNull Location location, @NotNull BlockData data, boolean global, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        Objects.requireNonNull(data);
        Objects.requireNonNull(allowedViewers);
        this.data = SpigotConversionUtil.fromBukkitBlockData(data.clone());
        this.world = location.getWorld();
        this.global = global;
        this.vector = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.location = location;
        this.allowedViewers.addAll(allowedViewers);
    }

    public void playAction(BlockAction action) {
        this.currentViewers.forEach(player -> this.playAction(player, action));
    }

    public void playAction(Player player, BlockAction action) {
        PacketFactory.INSTANCE.consume(factory -> factory.playBlockAction(player, this, action));
    }

    private void display(Player player, WrapperPlayClientPlayerDigging wrapper) {
        PacketFactory.INSTANCE.consume(factory -> {
            factory.placeBlock(player, this);
            if (wrapper == null) return;
            factory.acknowledgeBlockChanges(player, wrapper.getSequence());
        });
    }

    public void setData(BlockData data) {
        this.data = SpigotConversionUtil.fromBukkitBlockData(data);
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
    }

    @Override
    public void destroy(Player player) {
        PacketFactory.INSTANCE.consume(factory -> factory.breakBlock(player, this));
    }

    public Vector3i getVector() {
        return new Vector3i(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
    }

    public WrappedBlockState getOriginalData() {
        return SpigotConversionUtil.fromBukkitBlockData(this.location.getBlock().getBlockData());
    }

    public BlockData getBukkitData() {
        return SpigotConversionUtil.toBukkitBlockData(this.data);
    }

    @Override
    public Location getLocation() {
        return new Location(world, this.vector.x, this.vector.y, this.vector.z);
    }

    public static final class Builder {
        private final PacketBlock block;

        public Builder(@NotNull Location location, @NotNull BlockData data) {
            this.block = new PacketBlock(location, data, true);
        }

        public @NotNull Builder global(boolean global) {
            this.block.setGlobal(global);
            return this;
        }

        public @NotNull Builder interactionHandler(@NotNull InteractionHandler interactionHandler) {
            Objects.requireNonNull(interactionHandler);
            this.block.setInteractionHandler(interactionHandler);
            return this;
        }

        public @NotNull Builder viewers(@NotNull Player... viewers) {
            Objects.requireNonNull(viewers);
            this.block.setGlobal(false);
            this.block.allowedViewers.addAll(Arrays.stream(viewers).toList());
            return this;
        }

        public @NotNull Builder data(@NotNull BlockData data) {
            Objects.requireNonNull(data);
            this.block.setData(data);
            return this;
        }

        public @NotNull PacketBlock build() {
            return this.block;
        }
    }

}