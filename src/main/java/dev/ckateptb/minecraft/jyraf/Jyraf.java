package dev.ckateptb.minecraft.jyraf;

import dev.ckateptb.minecraft.jyraf.container.ReactiveContainer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

public class Jyraf extends JavaPlugin {
    private static final AtomicReference<SyncScheduler> CACHED_SYNC = new AtomicReference<>();
    private static Jyraf plugin;

    public Jyraf() {
        Jyraf.plugin = this;
        new ReactiveContainer().scan(this);
    }

    @Override
    public void onEnable() {
        System.out.println("Hello World");
    }

    public static Scheduler syncScheduler() {
        SyncScheduler syncScheduler = CACHED_SYNC.get();
        if (syncScheduler != null) return syncScheduler;
        syncScheduler = new SyncScheduler();
        CACHED_SYNC.set(syncScheduler);
        return syncScheduler;
    }

    public static class SyncScheduler implements Scheduler {
        @Override
        public Disposable schedule(Runnable task) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    task.run();
                }
            };

            if (Bukkit.isPrimaryThread()) runnable.run();
            else runnable.runTaskLater(Jyraf.plugin, 0);
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
        public Worker createWorker() {
            return new Worker() {
                @Override
                public Disposable schedule(Runnable task) {
                    return SyncScheduler.this.schedule(task);
                }

                @Override
                public void dispose() {
                    SyncScheduler.this.dispose();
                }
            };
        }
    }
}