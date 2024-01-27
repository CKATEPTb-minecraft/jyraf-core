package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.joor.Reflect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class WorldService {
    private final AsyncCache<World, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<Integer, Entity> entityIdCache = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<World, Supplier<Iterable<?>>> worldEntityIteratorCache = Caffeine.newBuilder().buildAsync();

    public Mono<Entity> getEntityById(World world, int entityId) {
        return Optional.ofNullable(this.entityIdCache.getIfPresent(entityId))
                .map(Mono::fromFuture)
                .orElse(Mono.empty())
                .switchIfEmpty(Mono.defer(() -> Mono.fromFuture(this.worldEntityIteratorCache.get(world, (key) ->
                                this.getEntityIterable(world)))
                        .<Iterable<?>>handle((supplier, sink) -> {
                            try {
                                sink.next(supplier.get());
                            } catch (Exception e) {
                                sink.error(e);
                            }
                        })
                        .flatMapMany(nmsEntitiesIterable -> Flux.fromIterable(nmsEntitiesIterable)
                                .map(SpigotReflectionUtil::getBukkitEntity))
                        .filter(entity -> entity.getEntityId() == entityId)
                        .singleOrEmpty()));
    }

    private Supplier<Iterable<?>> getEntityIterable(World world) {
        /*TODO Это только для Paper*/ if (SpigotReflectionUtil.V_1_19_OR_HIGHER) return this.getEntityIterable1_19(world);
        if (SpigotReflectionUtil.V_1_17_OR_HIGHER) return this.getEntityIterable1_17(world);
        throw new NotImplementedException("Not implement yet!");
        /*TODO 1.16.5*/
    }

    private Supplier<Iterable<?>> getEntityIterable1_19(World world) {
        try {
            Reflect entityLookup = Reflect.on(world)
                    .call("getHandle")
                    .call("getEntityLookup");
            Class<?> type = entityLookup.type();
            Method method = Arrays.stream(type.getMethods())
                    .filter(value -> Iterable.class.isAssignableFrom(value.getReturnType()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Iterable method is missing"));
            return () -> {
                try {
                    return (Iterable<?>) method.invoke(entityLookup.get());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Exception exception) {
            return this.getEntityIterable1_17(world);
        }
    }

    private Supplier<Iterable<?>> getEntityIterable1_17(World world) {
        Tuple2<Object, Method> objects = Reflect.on(world).call("getHandle")
                .fields()
                .entrySet()
                .stream()
                .filter(entry -> {
                    Reflect field = entry.getValue();
                    Class<?> type = field.type();
                    return type.equals(SpigotReflectionUtil.PERSISTENT_ENTITY_SECTION_MANAGER_CLASS);
                })
                .findFirst()
                .map(Map.Entry::getValue)
                .map(entitySection -> entitySection.fields()
                        .values()
                        .stream()
                        .filter(entityGetter -> entityGetter.type()
                                .equals(SpigotReflectionUtil.LEVEL_ENTITY_GETTER_CLASS))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(SpigotReflectionUtil.LEVEL_ENTITY_GETTER_CLASS + " is missing"))
                )
                .map(entityGetter -> {
                    Class<?> entityGetterType = entityGetter.type();
                    return Tuples.of(entityGetter.get(), Arrays.stream(entityGetterType.getMethods())
                            .filter(method -> Iterable.class.isAssignableFrom(method.getReturnType()))
                            .findFirst().orElseThrow(() -> new RuntimeException("Iterable method is missing")));
                })
                .orElseThrow(() -> new RuntimeException(SpigotReflectionUtil.PERSISTENT_ENTITY_SECTION_MANAGER_CLASS + " is missing"));
        return () -> {
            try {
                return (Iterable<?>) objects.getT2().invoke(objects.getT1());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
