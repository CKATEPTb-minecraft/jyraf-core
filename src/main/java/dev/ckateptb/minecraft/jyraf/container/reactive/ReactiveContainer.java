package dev.ckateptb.minecraft.jyraf.container.reactive;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.reflect.ClassPath;
import dev.ckateptb.minecraft.jyraf.container.annotation.Autowired;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.container.annotation.PostConstruct;
import dev.ckateptb.minecraft.jyraf.container.annotation.Qualifier;
import dev.ckateptb.minecraft.jyraf.container.api.AsyncContainer;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.container.handler.ContainerInitializeHandler;
import dev.ckateptb.minecraft.jyraf.listener.PluginEnableListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReactiveContainer implements AsyncContainer {
    private final AsyncCache<BeanKey<?>, Object> beans = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<BeanKey<?>, Plugin> owners = Caffeine.newBuilder().buildAsync();
    private final ConcurrentLinkedQueue<ComponentRegisterHandler> componentRegisterHandlers = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ContainerInitializeHandler> containerInitializedHandlers = new ConcurrentLinkedQueue<>();
    @Getter
    private final String name;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Mono<T>> getBean(Class<T> beanClass, String qualifier) {
        return Optional.ofNullable(this.beans.getIfPresent(new BeanKey<>(beanClass, qualifier)))
                .map(future -> Mono.fromFuture((CompletableFuture<T>) future));
    }

    public <T> Optional<Mono<Plugin>> getOwner(Class<T> beanClass, String qualifier) {
        return Optional.ofNullable(this.owners.getIfPresent(new BeanKey<>(beanClass, qualifier)))
                .map(Mono::fromFuture);
    }

    @Override
    public void addComponentRegisterHandler(ComponentRegisterHandler handler) {
        this.componentRegisterHandlers.add(handler);
    }

    @Override
    public void removeComponentRegisterHandler(ComponentRegisterHandler handler) {
        this.componentRegisterHandlers.remove(handler);
    }

    @Override
    public void addContainerInitializedHandler(ContainerInitializeHandler handler) {
        this.containerInitializedHandlers.add(handler);
    }

    @Override
    public void removeContainerInitializedHandler(ContainerInitializeHandler handler) {
        this.containerInitializedHandlers.remove(handler);
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
                    .forEach(component -> this.registerOwners(plugin, new BeanKey<>(
                            component,
                            component.getAnnotation(Component.class).value()
                    ), new LinkedList<>()));
        }
    }

    @Override
    public void initialize() {
        Flux.fromIterable(this.owners.asMap().keySet())
                .flatMap(this::registerBean)
                .count()
                .subscribe(count -> containerInitializedHandlers.forEach(handler -> handler.handle(this, count)));
    }

    private <T> Mono<T> registerBean(BeanKey<T> key) {
        Class<T> clazz = key.clazz();
        String qualifier = key.qualifier();
        Optional<Mono<T>> optional = this.getBean(clazz, qualifier);
        if (optional.isPresent()) return optional.get();
        Constructor<T> constructor = this.findConstructor(clazz);
        return Flux.fromIterable(this.findParameters(constructor))
                .flatMap(this::registerBean)
                .collectList()
                .mapNotNull(objects -> {
                    try {
                        return constructor.newInstance(objects.toArray());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .doOnNext(bean -> {
                    Class<?> beanClass = bean.getClass();
                    Method[] declaredMethods = beanClass.getDeclaredMethods();
                    for (Method method : declaredMethods) {
                        if (method.isAnnotationPresent(PostConstruct.class)) {
                            method.setAccessible(true);
                            if (method.getParameters().length > 0) {
                                throw new RuntimeException("A method " + method.getName() +
                                        " annotated with @PostConstruct in class " + beanClass +
                                        "must not contain parameters.");
                            }
                            try {
                                method.invoke(bean);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                    }
                })
                .zipWith(Mono.defer(() -> Mono.fromFuture(this.owners.asMap().get(key))))
                .doOnNext(tuple -> {
                    T bean = tuple.getT1();
                    Plugin plugin = tuple.getT2();
                    this.registerBean(plugin, bean, qualifier);
                    this.handleHandlers(bean, qualifier, plugin);
                })
                .map(Tuple2::getT1);
    }

    private <T> void handleHandlers(T bean, String qualifier, Plugin plugin) {
        this.componentRegisterHandlers.forEach(handler -> this.executeWhenEnable(plugin, () -> {
            try {
                handler.handle(bean, qualifier, plugin);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }));
    }

    private void executeWhenEnable(Plugin plugin, Runnable runnable) {
        if (plugin.isEnabled()) runnable.run();
        else PluginEnableListener.getExecuteOnEnable().computeIfAbsent(plugin, key -> new HashSet<>()).add(runnable);
    }

    private <T> void registerOwners(Plugin plugin, BeanKey<T> key, Deque<BeanKey<?>> stacktrace) {
        if (stacktrace.contains(key)) {
            stacktrace.push(key);
            throw new RuntimeException("A circular dependency was detected for the class " + key.clazz().getName() +
                    " with qualifier \"" + key.qualifier() + "\": " + stacktrace
                    .stream()
                    .map(beanKey -> beanKey.clazz().toString() + " \"" + beanKey.qualifier() + "\"")
                    .collect(Collectors.joining(" <- "))
            );
        }
        if (this.owners.asMap().containsKey(key)) return;
        stacktrace.push(key);
        this.owners.put(key, CompletableFuture.completedFuture(plugin));
        Constructor<T> constructor = this.findConstructor(key.clazz());
        for (BeanKey<?> beanKey : findParameters(constructor)) {
            this.registerOwners(plugin, beanKey, stacktrace);
        }
        stacktrace.pop();
    }

    private <T> List<BeanKey<?>> findParameters(Constructor<T> constructor) {
        List<BeanKey<?>> keys = new ArrayList<>();
        Parameter[] parameters = constructor.getParameters();
        for (Parameter parameter : parameters) {
            Class<?> component = parameter.getType();
            keys.add(this.getBeanKey(parameter, component));
        }
        return keys;
    }

    private BeanKey<?> getBeanKey(Parameter parameter, Class<?> component) {
        AtomicReference<String> qualifier = new AtomicReference<>(Qualifier.DEFAULT_QUALIFIER);
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
