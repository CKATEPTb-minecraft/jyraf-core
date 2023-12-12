package dev.ckateptb.minecraft.jyraf;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.IoC;
import dev.ckateptb.minecraft.jyraf.schedule.Schedule;
import dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import reactor.core.scheduler.Scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Jyraf extends JavaPlugin {
    private final static Cache<Plugin, SyncScheduler> SCHEDULER_CACHE = Caffeine.newBuilder().build();
    private final static Cache<Plugin, AnnotationParser<CommandSender>> COMMAND_CACHE = Caffeine.newBuilder().build();

    @Getter
    private static Jyraf plugin;

    public Jyraf() {
        Jyraf.plugin = this;
        IoC.addCallback((component, qualifier, owner) -> {
            // Listeners
            if (component instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, owner);
            }
            Class<?> clazz = component.getClass();
            // Commands
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Schedule.class)) continue;
                method.setAccessible(true);
                try {
                    Schedule annotation = method.getAnnotation(Schedule.class);
                    int fixedRate = annotation.fixedRate();
                    int initialDelay = annotation.initialDelay();
                    boolean async = annotation.async();
                    BukkitScheduler scheduler = Bukkit.getScheduler();
                    Method task = scheduler.getClass()
                            .getDeclaredMethod(async ? "runTaskTimerAsynchronously" : "runTaskTimer",
                                    Plugin.class,
                                    Runnable.class,
                                    long.class,
                                    long.class);
                    task.setAccessible(true);
                    task.invoke(scheduler, plugin, (Runnable) () -> {
                        try {
                            method.invoke(component);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }, initialDelay, fixedRate);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            // Commands
            if (component instanceof Command command) {
                try {
                    AnnotationParser<CommandSender> parser = COMMAND_CACHE.get(owner, plugin -> {
                        try {
                            BukkitCommandManager<CommandSender> manager = BukkitCommandManager
                                    .createNative(plugin, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator());
                            return new AnnotationParser<>(
                                    manager,
                                    CommandSender.class,
                                    sender -> SimpleCommandMeta.builder()
                                            .with(CommandMeta.DESCRIPTION, "No description")
                                            .build()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    ParserRegistry<CommandSender> registry = parser.manager().parserRegistry();
                    command.getParsers().forEach(registry::registerParserSupplier);
                    parser.parse(command);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        IoC.scan(this);
    }

    public static Scheduler syncScheduler(Plugin plugin) {
        return SCHEDULER_CACHE.get(plugin, SyncScheduler::new);
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTask(this, IoC::initialize);
    }

    public Scheduler syncScheduler() {
        return syncScheduler(this);
    }
}