package dev.ckateptb.minecraft.jyraf.container.api;

import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import dev.ckateptb.minecraft.jyraf.container.callback.ContainerInitializedCallback;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.function.Predicate;

import static dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier.DEFAULT_QUALIFIER;

public interface Container {

    default <T> Optional<?> getBean(Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<?> getBean(Class<T> beanClass, String qualifier);

    default <T> void registerBean(Plugin plugin, T bean) {
        this.registerBean(plugin, bean, DEFAULT_QUALIFIER);
    }

    <T> void registerBean(Plugin plugin, T bean, String qualifier);

    default <T> boolean containsBean(Class<T> beanClass) {
        return this.containsBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> boolean containsBean(Class<T> beanClass, String qualifier);

    default <P extends Plugin> void scan(P plugin, String... packages) {
        this.scan(plugin, path -> true, packages);
    }

    default <P extends Plugin> void scan(P plugin, Predicate<String> filter) {
        this.scan(plugin, filter, plugin.getClass().getPackageName());
    }

    <P extends Plugin> void scan(P plugin, Predicate<String> filter, String... packages);

    default <T> Optional<?> getOwner(Class<T> beanClass) {
        return this.getOwner(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<?> getOwner(Class<T> beanClass, String qualifier);

    void addComponentRegisterCallback(ComponentRegisterCallback callback);

    void removeComponentRegisterCallback(ComponentRegisterCallback callback);

    void addContainerInitializedCallback(ContainerInitializedCallback callback);

    void removeContainerInitializedCallback(ContainerInitializedCallback callback);

    void initialize();

    String getName();
}
