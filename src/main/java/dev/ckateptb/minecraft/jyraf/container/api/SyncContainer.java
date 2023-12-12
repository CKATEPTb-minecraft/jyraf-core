package dev.ckateptb.minecraft.jyraf.container.api;

import org.bukkit.plugin.Plugin;

import java.util.Optional;

import static dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier.DEFAULT_QUALIFIER;

public interface SyncContainer extends Container {
    default <T> Optional<T> getBean(Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<T> getBean(Class<T> beanClass, String qualifier);

    default <T> Optional<Plugin> getOwner(Class<T> beanClass) {
        return this.getOwner(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<Plugin> getOwner(Class<T> beanClass, String qualifier);
}
