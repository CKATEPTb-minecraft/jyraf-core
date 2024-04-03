package dev.ckateptb.minecraft.jyraf.container;

import dev.ckateptb.minecraft.jyraf.container.api.AsyncContainer;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.container.handler.ContainerInitializeHandler;
import dev.ckateptb.minecraft.jyraf.container.reactive.ReactiveContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class IoC {
    private static final AsyncContainer CONTAINER = new ReactiveContainer("GLOBAL");

    public static <T> @NotNull Optional<Mono<T>> getBean(@NotNull Class<T> beanClass) {
        return CONTAINER.getBean(beanClass);
    }

    public static <T> void registerBean(@NotNull Plugin plugin, @NotNull T bean) {
        CONTAINER.registerBean(plugin, bean);
    }

    public static <T> boolean containsBean(@NotNull Class<T> beanClass) {
        return CONTAINER.containsBean(beanClass);
    }

    public static <P extends Plugin> void scan(@NotNull P plugin, @NotNull String... packages) {
        CONTAINER.scan(plugin, packages);
    }

    public static <P extends Plugin> void scan(@NotNull P plugin, @NotNull Predicate<String> filter) {
        CONTAINER.scan(plugin, filter);
    }

    public static <T> @NotNull Optional<?> getOwner(@NotNull Class<T> beanClass) {
        return CONTAINER.getOwner(beanClass);
    }

    public static <T> @NotNull Optional<Mono<T>> getBean(@NotNull Class<T> beanClass, @NotNull String qualifier) {
        return CONTAINER.getBean(beanClass, qualifier);
    }

    public static <T> @NotNull Optional<Mono<Plugin>> getOwner(@NotNull Class<T> beanClass, @NotNull String qualifier) {
        return CONTAINER.getOwner(beanClass, qualifier);
    }

    public static void addComponentRegisterHandler(@NotNull ComponentRegisterHandler handler) {
        CONTAINER.addComponentRegisterHandler(handler);
    }

    public static void removeComponentRegisterHandler(@NotNull ComponentRegisterHandler handler) {
        Objects.requireNonNull(handler);
        CONTAINER.removeComponentRegisterHandler(handler);
    }

    public static void addContainerInitializedHandler(@NotNull ContainerInitializeHandler handler) {
        Objects.requireNonNull(handler);
        CONTAINER.addContainerInitializedHandler(handler);
    }

    public static void removeContainerInitializedHandler(@NotNull ContainerInitializeHandler handler) {
        Objects.requireNonNull(handler);
        CONTAINER.removeContainerInitializedHandler(handler);
    }

    public static <T> void registerBean(@NotNull Plugin plugin, @NotNull T bean, @NotNull String qualifier) {
        CONTAINER.registerBean(plugin, bean, qualifier);
    }

    public static <T> boolean containsBean(@NotNull Class<T> beanClass, @NotNull String qualifier) {
        return CONTAINER.containsBean(beanClass, qualifier);
    }

    public static <P extends Plugin> void scan(@NotNull P plugin, @NotNull Predicate<String> filter, @NotNull String... packages) {
        CONTAINER.scan(plugin, filter, packages);
    }

    public static void initialize() {
        CONTAINER.initialize();
    }
}
