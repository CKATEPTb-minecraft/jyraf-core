package dev.ckateptb.minecraft.jyraf.packet.trait;

import dev.ckateptb.minecraft.jyraf.packet.PacketEntry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

@Getter
@Setter
public abstract class PacketTrait<T extends PacketEntry> {

    private final EventPriority priority;
    private boolean cancelled;

    public PacketTrait() {
        this(EventPriority.NORMAL);
    }

    public PacketTrait(EventPriority priority) {
        this.priority = priority;
    }

    public abstract Class<T> getEntryClass();

    public abstract void tick(Player player, T packetEntry);

}