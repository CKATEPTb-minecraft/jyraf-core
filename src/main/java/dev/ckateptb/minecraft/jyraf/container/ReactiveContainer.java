package dev.ckateptb.minecraft.jyraf.container;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.reflect.ClassPath;
import dev.ckateptb.minecraft.jyraf.container.annotation.Autowired;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ReactiveContainer implements Container {
    private final AsyncCache<BeanKey<?>, Object> beans = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<BeanKey<?>, Plugin> owners = Caffeine.newBuilder().buildAsync();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Mono<T>> getBean(Class<T> beanClass, String qualifier) {
        return Optional.ofNullable(this.beans.getIfPresent(new BeanKey<>(beanClass, qualifier)))
                .map(future -> Mono.fromFuture((CompletableFuture<T>) future));
    }

    @Override
    public <T> void registerBean(Plugin plugin, T bean, String qualifier) {
        BeanKey<?> key = new BeanKey<>(bean.getClass(), qualifier);
        this.beans.put(key, CompletableFuture.completedFuture(bean));
        this.owners.put(key, CompletableFuture.completedFuture(plugin));
    }

    @Override
    public <T> boolean containsBean(Class<T> beanClass, String qualifier) {
        return this.getBean(beanClass, qualifier).isPresent();
    }

    @Override
    @SneakyThrows
    public <P extends Plugin> void scan(P plugin, Predicate<String> filter, String... packages) {
        this.registerBean(plugin, plugin);
        Class<? extends Plugin> pluginClass = plugin.getClass();
        ClassLoader classLoader = pluginClass.getClassLoader();
        ClassPath classPath = ClassPath.from(classLoader);
        if (packages.length == 0) packages = new String[]{pluginClass.getPackageName()};
        for (String packageName : packages) {
            classPath.getTopLevelClassesRecursive(packageName).stream()
                    .filter(classInfo -> filter.test(classInfo.getName()))
                    .map(classInfo -> {
                        try {
                            return classInfo.load();
                        } catch (Exception ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                    .forEach(component -> {
                        Component annotation = component.getAnnotation(Component.class);
                        String qualifier = annotation.value();
                        BeanKey<?> key = new BeanKey<>(component, qualifier);
                        this.registerComponent(plugin, key, new LinkedList<>());
                    });
        }
        log.info(this.owners.asMap().keySet().stream().map(Objects::toString).collect(Collectors.joining("\n")));
    }

    private void registerComponent(Plugin plugin, BeanKey<?> key, Deque<BeanKey<?>> stacktrace) {
        if (stacktrace.contains(key)) {
            log.error("A circular dependency was detected for the class: {} with qualifier ({})", key.clazz().getName(), key.qualifier(), new RuntimeException());
            return;
        }
        if (this.owners.asMap().containsKey(key)) return;
        stacktrace.push(key);
        this.owners.put(key, CompletableFuture.completedFuture(plugin));
        this.registerParameters(plugin, this.findConstructor(key.clazz()), stacktrace);
        stacktrace.pop();
    }

    private void registerParameters(Plugin plugin, Constructor<?> constructor, Deque<BeanKey<?>> stacktrace) {
        Parameter[] parameters = constructor.getParameters();
        for (Parameter parameter : parameters) {
            Class<?> component = parameter.getType();
            BeanKey<?> key = getBeanKey(parameter, component);
            if (this.owners.asMap().containsKey(key)) continue;
            this.registerComponent(plugin, key, stacktrace);
        }
    }

    private BeanKey<?> getBeanKey(Parameter parameter, Class<?> component) {
        AtomicReference<String> qualifier = new AtomicReference<>(Container.DEFAULT_QUALIFIER);
        Qualifier qualifierAnnotation = parameter.getAnnotation(Qualifier.class);
        if (qualifierAnnotation != null) {
            qualifier.set(qualifierAnnotation.value());
        } else {
            Component componentAnnotation = component.getAnnotation(Component.class);
            if (componentAnnotation != null) qualifier.set(componentAnnotation.value());
        }
        return new BeanKey<>(component, qualifier.get());
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findConstructor(Class<T> component) {
        Constructor<T>[] constructors = (Constructor<T>[]) component.getConstructors();
        Constructor<T> constructor = Arrays.stream(constructors)
                .filter(value -> value.isAnnotationPresent(Autowired.class))
                .findFirst().orElseGet(() -> constructors[0]);
        constructor.setAccessible(true);
        return constructor;
    }
}
