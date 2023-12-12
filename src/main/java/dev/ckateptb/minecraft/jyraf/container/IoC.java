package dev.ckateptb.minecraft.jyraf.container;

import dev.ckateptb.minecraft.jyraf.container.api.AsyncContainer;
import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import dev.ckateptb.minecraft.jyraf.container.reactive.ReactiveContainer;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Predicate;

public class IoC {
    private static final AsyncContainer CONTAINER = new ReactiveContainer();

    public static <T> Optional<Mono<T>> getBean(Class<T> beanClass) {
        return CONTAINER.getBean(beanClass);
    }

    public static <T> void registerBean(Plugin plugin, T bean) {
        CONTAINER.registerBean(plugin, bean);
    }

    public static <T> boolean containsBean(Class<T> beanClass) {
        return CONTAINER.containsBean(beanClass);
    }

    public static <P extends Plugin> void scan(P plugin, String... packages) {
        CONTAINER.scan(plugin, packages);
    }

    public static <P extends Plugin> void scan(P plugin, Predicate<String> filter) {
        CONTAINER.scan(plugin, filter);
    }

    public static <T> Optional<?> getOwner(Class<T> beanClass) {
        return CONTAINER.getOwner(beanClass);
    }

    public static <T> Optional<Mono<T>> getBean(Class<T> beanClass, String qualifier) {
        return CONTAINER.getBean(beanClass, qualifier);
    }

    public static <T> Optional<Mono<Plugin>> getOwner(Class<T> beanClass, String qualifier) {
        return CONTAINER.getOwner(beanClass, qualifier);
    }

    public static void addCallback(ComponentRegisterCallback callback) {
        CONTAINER.addCallback(callback);
    }

    public static void removeCallback(ComponentRegisterCallback callback) {
        CONTAINER.removeCallback(callback);
    }

    public static <T> void registerBean(Plugin plugin, T bean, String qualifier) {
        CONTAINER.registerBean(plugin, bean, qualifier);
    }

    public static <T> boolean containsBean(Class<T> beanClass, String qualifier) {
        return CONTAINER.containsBean(beanClass, qualifier);
    }

    public static <P extends Plugin> void scan(P plugin, Predicate<String> filter, String... packages) {
        CONTAINER.scan(plugin, filter, packages);
    }

    public static void initialize() {
        CONTAINER.initialize();
    }
}
