package dev.ckateptb.minecraft.jyraf.packet.trait;

import dev.ckateptb.minecraft.jyraf.packet.PacketEntry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public abstract class PacketTrait<T extends PacketEntry> {

    @NotNull
    private final EventPriority priority;
    private boolean cancelled;

    public PacketTrait() {
        this(EventPriority.NORMAL);
    }

    public PacketTrait(@NotNull EventPriority priority) {
        Objects.requireNonNull(priority);
        this.priority = priority;
    }

    public abstract @NotNull Class<T> getEntryClass();

    public void tick(@NotNull Mono<List<Player>> players, @NotNull T packetEntry) {

    }

    public void tick(@NotNull Player player, @NotNull T packetEntry) {

    }

}