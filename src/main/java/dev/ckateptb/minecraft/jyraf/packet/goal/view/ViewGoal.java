package dev.ckateptb.minecraft.jyraf.packet.goal.view;

import dev.ckateptb.minecraft.jyraf.packet.goal.PacketGoal;
import dev.ckateptb.minecraft.jyraf.packet.managed.RepositoryManaged;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class ViewGoal extends PacketGoal<RepositoryManaged> {
    public ViewGoal() {
        this(Priority.LOWEST);
    }

    public ViewGoal(Priority priority) {
        super(priority);
    }

    @Override
    public Result onTick(RepositoryManaged entry, Player... players) {
        entry.getCurrentViewers().removeIf(player -> {
            if (Arrays.asList(players).contains(player) && player.isOnline()) return false;
            entry.despawn(Collections.singleton(player));
            return true;
        });
        for (Player player : players) {
            if (entry.getCurrentViewers().add(player)) {
                entry.spawn(Collections.singleton(player));
            }
        }
        return Result.CONTINUE;
    }
}
