package dev.ckateptb.minecraft.jyraf.container.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier.DEFAULT_QUALIFIER;

public interface AsyncContainer extends Container {
    default <T> @NotNull Optional<Mono<T>> getBean(@NotNull Class<T> beanClass) {
        return this.getBean(beanClass, DEFAULT_QUALIFIER);
    }

    <T> @NotNull Optional<Mono<T>> getBean(@NotNull Class<T> beanClass, @NotNull String qualifier);

    default <T> @NotNull Optional<Mono<Plugin>> getOwner(Class<T> beanClass) {
        return this.getOwner(beanClass, DEFAULT_QUALIFIER);
    }

    <T> @NotNull Optional<Mono<Plugin>> getOwner(@NotNull Class<T> beanClass, @NotNull String qualifier);

}
