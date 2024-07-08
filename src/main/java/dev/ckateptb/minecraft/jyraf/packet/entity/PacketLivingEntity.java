package dev.ckateptb.minecraft.jyraf.packet.entity;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.LivingEntityMeta;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.patheloper.api.pathing.strategy.strategies.WalkablePathfinderStrategy;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class PacketLivingEntity extends PacketEntity {
    // 0 = main hand, 1 = offhand, 2 = boots, 3 = leggings, 4 = chestplate, 5 = helmet
    private final ItemStack[] equipment = new ItemStack[6];

    protected PacketLivingEntity(int id, UUID uuid, EntityType type, LivingEntityMeta meta, Location location) {
        super(id, uuid, type, meta, location);
        Arrays.fill(this.equipment, ItemStack.EMPTY);
    }

    @Override
    public void refresh(Collection<Player> players) {
        super.refresh(players);
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.equipEntity(player, this);
            }
        });
    }

    @Override
    public LivingEntityMeta getMeta() {
        return (LivingEntityMeta) super.getMeta();
    }

    @Override
    public Mono<Boolean> moveTo(Location location) {
        return this.moveTo(location, new WalkablePathfinderStrategy());
    }

    public void setItem(@NotNull EquipmentSlot slot, @NotNull org.bukkit.inventory.ItemStack itemStack) {
        this.equipment[slot.ordinal()] = SpigotConversionUtil.fromBukkitItemStack(itemStack);
    }

    public @NotNull org.bukkit.inventory.ItemStack getItem(@NotNull EquipmentSlot slot) {
        int ordinal = slot.ordinal();
        if(this.equipment.length <= ordinal) return new org.bukkit.inventory.ItemStack(Material.AIR);
        ItemStack stack = this.equipment[ordinal];
        if(stack == null) return new org.bukkit.inventory.ItemStack(Material.AIR);
        return SpigotConversionUtil.toBukkitItemStack(stack);
    }

    public void animation(WrapperPlayServerEntityAnimation.EntityAnimationType animation, Player... players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.animationEntity(player, this, animation);
            }
        });
    }

    public void potion(PotionType type, int amplifier, int duration, byte flags, Player... players) {
        PacketFactory.INSTANCE.consume(factory -> {
            for (Player player : players) {
                factory.potionEntity(player, this, type, amplifier, duration, flags);
            }
        });
    }
}
