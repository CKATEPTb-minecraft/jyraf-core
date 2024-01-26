package dev.ckateptb.minecraft.jyraf.packet.block;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import dev.ckateptb.minecraft.jyraf.packet.model.PacketModel;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.experimental.Delegate;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

// TODO Реализовать нормальную систему фейковых блоков
public class PacketBlock extends PacketModel {
    @Delegate
    private WrappedBlockState state;

    public PacketBlock(Block block) {
        this.location = block.getLocation();
        this.setBlockData(block.getBlockData());
    }

    public void setBlockData(BlockData data) {
        this.state = SpigotConversionUtil.fromBukkitBlockData(data);
    }

    public void update() {
        this.getAudience()
                .stream()
                .filter(this::canView) // TODO понять нужен ли этот фильтр
                .forEach(this::onShow);
    }

    public void revert() {
        this.getAudience()
                .forEach(this::hide); // TODO может ли быть конкурент?
    }

    @Override
    protected void onShow(Player player) {
        PlayerManager manager = PacketEvents.getAPI().getPlayerManager();
        manager.sendPacket(player, new WrapperPlayServerBlockChange(
                new Vector3i(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ()),
                this.state.getGlobalId()
        ));
    }

    @Override
    protected void onHide(Player player) {
        PlayerManager manager = PacketEvents.getAPI().getPlayerManager();
        manager.sendPacket(player, new WrapperPlayServerBlockChange(
                new Vector3i(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ()),
                SpigotConversionUtil.fromBukkitBlockData(this.location.getBlock().getBlockData()).getGlobalId()
        ));
    }
}
