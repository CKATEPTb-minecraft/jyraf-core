package dev.ckateptb.minecraft.jyraf.schedule.inject;

import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;

public class ScheduleInjection implements ComponentRegisterCallback {
    @Override
    @SneakyThrows
    public void handle(Object component, String qualifier, Plugin owner) {
        Class<?> clazz = component.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Schedule.class)) continue;
            method.setAccessible(true);
            Schedule annotation = method.getAnnotation(Schedule.class);
            int fixedRate = annotation.fixedRate();
            int initialDelay = annotation.initialDelay();
            boolean async = annotation.async();
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                @SneakyThrows
                public void run() {
                    method.invoke(component);
                }
            };
            if (async) task.runTaskTimerAsynchronously(owner, initialDelay, fixedRate);
            else task.runTaskTimer(owner, initialDelay, fixedRate);
        }
    }


}
