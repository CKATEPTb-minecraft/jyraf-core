package dev.ckateptb.minecraft.jyraf.container.api;

import dev.ckateptb.minecraft.jyraf.internal.reactor.core.publisher.Mono;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

import static dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier.DEFAULT_QUALIFIER;

public interface AsyncContainer extends Container {
    default <T> Optional<Mono<T>> getBean(Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<Mono<T>> getBean(Class<T> beanClass, String qualifier);

    default <T> Optional<Mono<Plugin>> getOwner(Class<T> beanClass) {
        return this.getOwner(beanClass, DEFAULT_QUALIFIER);
    }

    <T> Optional<Mono<Plugin>> getOwner(Class<T> beanClass, String qualifier);

}
