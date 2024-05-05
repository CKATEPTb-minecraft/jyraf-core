package dev.ckateptb.minecraft.jyraf.packet.block;

import com.github.retrooper.packetevents.util.Vector3i;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.packet.goal.view.ViewGoal;
import dev.ckateptb.minecraft.jyraf.packet.managed.RepositoryManaged;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.entity.Player;

import java.util.Collection;

@Getter
public class PacketBlock extends RepositoryManaged {
    protected final BlockData data;
    protected final Location location;

    protected PacketBlock(Location location, BlockData data) {
        this.location = location.clone();
        this.data = data;
        this.addGoal(new ViewGoal());
    }

    public static PacketBlock of(BlockData data, Location location) {
        if (data instanceof Chest || data instanceof EnderChest) {
            return new PacketChestBlock(location, data);
        }
        if (data instanceof Piston || data instanceof TechnicalPiston) {
            return new PacketPistonBlock(location, data);
        }
        Material material = data.getMaterial();
        if (material.name().contains("SHULKER_BOX")) {
            return new PacketShulkerBlock(location, data);
        }
        return switch (material) {
            case BELL -> new PacketBellBlock(location, data);
            case NOTE_BLOCK -> new PacketNoteBlock(location, data);
            case END_GATEWAY -> new PacketGatewayBlock(location, data);
            case SPAWNER -> new PacketSpawnerBlock(location, data);
            default -> new PacketBlock(location, data);
        };
    }

    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    public Vector3i getVector3i() {
        return new Vector3i(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
    }

    @Override
    public void spawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeSpawn(this, players.toArray(new Player[0])));
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.placeBlock(player, this);
            }
        });
        this.getGoals().forEach(goal -> goal.onSpawn(this, players.toArray(new Player[0])));
    }

    @Override
    public void despawn(Collection<Player> players) {
        this.getGoals().forEach(goal -> goal.beforeDespawn(this, players.toArray(new Player[0])));
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.breakBlock(player, this);
            }
        });
        this.getGoals().forEach(goal -> goal.onDespawn(this, players.toArray(new Player[0])));
    }
}
