package dev.ckateptb.minecraft.jyraf.container;


import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Predicate;

public interface Container {
    public static final String DEFAULT_QUALIFIER = "";

    default <T> Optional<Mono<T>> getBean(Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<Mono<T>> getBean(Class<T> beanClass, String qualifier);

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
}
