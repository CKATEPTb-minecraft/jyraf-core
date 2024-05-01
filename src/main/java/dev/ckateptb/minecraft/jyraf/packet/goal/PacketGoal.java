package dev.ckateptb.minecraft.jyraf.packet.goal;

import dev.ckateptb.minecraft.jyraf.packet.managed.RepositoryManaged;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public abstract class PacketGoal<T extends RepositoryManaged> {
    private final Priority priority;

    public void beforeSpawn(T entry, Player... players) {
    }

    public void onSpawn(T entry, Player... players) {
    }

    public Result onTick(T entry, Player... players) {
        return Result.CONTINUE;
    }


    public void beforeDespawn(T entry, Player... players) {
    }

    public void onDespawn(T entry, Player... players) {
    }

    public enum Result {
        CONTINUE, //
        PENDING,
        DESTROY
    }

    public enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR
    }
}
