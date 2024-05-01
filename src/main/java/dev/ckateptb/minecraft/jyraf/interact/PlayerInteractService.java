package dev.ckateptb.minecraft.jyraf.interact;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.interact.event.PlayerInteractEvent;
import dev.ckateptb.minecraft.jyraf.packet.block.PacketBlock;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.enums.MouseButton;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;

@Component
public class PlayerInteractService {
    // Кешируем действие на 60 мс (чуть больше тика), чтобы предотвратить излишные вызовы ивента и
    // более точно преопределять что же игрок сделал
    private final AsyncCache<UUID, Boolean> cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(60))
            .buildAsync();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void tryFireInteract(Player player, MouseButton button, Block block, Entity entity,
                                PacketBlock packetBlock, PacketEntity packetEntity) {
        this.cache.get(player.getUniqueId(), key -> this.fireInteract(player, button, block, entity,
                packetBlock, packetEntity));
    }

    public boolean fireInteract(Player player, MouseButton button, Block block, Entity entity,
                                PacketBlock packetBlock, PacketEntity packetEntity) {
        return new PlayerInteractEvent(player, button, block, entity,
                packetBlock, packetEntity).callEvent();
    }
}
