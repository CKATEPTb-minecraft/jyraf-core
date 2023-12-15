package dev.ckateptb.minecraft.jyraf.example.scheduler;

import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.example.config.ConfigExample;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExampleScheduler {
    public final ConfigExample config;

    @Schedule(async = true, fixedRate = 100, initialDelay = 0)
    public void fiveSeconds() {
        if (!config.getDebug()) return;
        System.out.println("Wow, it's work=)");
    }
}
