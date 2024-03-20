package dev.ckateptb.minecraft.jyraf.schedule;

import dev.ckateptb.minecraft.jyraf.internal.reactor.core.Disposable;
import dev.ckateptb.minecraft.jyraf.internal.reactor.core.scheduler.Scheduler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class SyncScheduler implements Scheduler {
    private final Plugin plugin;

    @Override
    public @NonNull Disposable schedule(@NonNull Runnable task) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        if (Bukkit.isPrimaryThread()) runnable.run();
        else runnable.runTaskLater(this.plugin, 0);
        return new Disposable() {
            @Override
            public void dispose() {
                runnable.cancel();
            }

            @Override
            public boolean isDisposed() {
                return runnable.isCancelled();
            }
        };
    }

    @Override
    public @NonNull Worker createWorker() {
        return new Worker() {
            @Override
            public @NonNull Disposable schedule(@NonNull Runnable task) {
                return SyncScheduler.this.schedule(task);
            }

            @Override
            public void dispose() {
                SyncScheduler.this.dispose();
            }
        };
    }
}
