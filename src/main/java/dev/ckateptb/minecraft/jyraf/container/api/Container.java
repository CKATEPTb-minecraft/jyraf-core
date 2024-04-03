package dev.ckateptb.minecraft.jyraf.container.api;

import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.container.handler.ContainerInitializeHandler;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

import static dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier.DEFAULT_QUALIFIER;

public interface Container {

    default <T> @NotNull Optional<?> getBean(@NotNull Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> @NotNull Optional<?> getBean(@NotNull Class<T> beanClass, @NotNull String qualifier);

    default <T> void registerBean(@NotNull Plugin plugin, @NotNull T bean) {
        this.registerBean(plugin, bean, DEFAULT_QUALIFIER);
    }

    <T> void registerBean(@NotNull Plugin plugin, @NotNull T bean, @NotNull String qualifier);

    default <T> boolean containsBean(@NotNull Class<T> beanClass) {
        return this.containsBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> boolean containsBean(@NotNull Class<T> beanClass, @NotNull String qualifier);

    default <P extends Plugin> void scan(@NotNull P plugin, @NotNull String... packages) {
        this.scan(plugin, path -> true, packages);
    }

    default <P extends Plugin> void scan(@NotNull P plugin, @NotNull Predicate<String> filter) {
        this.scan(plugin, filter, plugin.getClass().getPackageName());
    }

    <P extends Plugin> void scan(@NotNull P plugin, @NotNull Predicate<String> filter, @NotNull String... packages);

    default <T> @NotNull Optional<?> getOwner(@NotNull Class<T> beanClass) {
        return this.getOwner(beanClass, DEFAULT_QUALIFIER);
    }

    <T> @NotNull Optional<?> getOwner(@NotNull Class<T> beanClass, @NotNull String qualifier);

    void addComponentRegisterHandler(@NotNull ComponentRegisterHandler handler);

    void removeComponentRegisterHandler(@NotNull ComponentRegisterHandler handler);

    void addContainerInitializedHandler(@NotNull ContainerInitializeHandler handler);

    void removeContainerInitializedHandler(@NotNull ContainerInitializeHandler handler);

    void initialize();

    String getName();
}
