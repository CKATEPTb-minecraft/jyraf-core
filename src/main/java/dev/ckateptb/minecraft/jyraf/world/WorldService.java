package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
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

@Component
public class WorldService {
    private final AsyncCache<World, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<Integer, Entity> entityIdCache = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<World, Tuple2<Object, Method>> worldEntityIteratorCache = Caffeine.newBuilder().buildAsync();

    public Mono<Entity> getEntityById(World world, int entityId) {
        return Optional.ofNullable(this.entityIdCache.getIfPresent(entityId))
                .map(Mono::fromFuture)
                .orElse(Mono.empty())
                .switchIfEmpty(Mono.defer(() -> Mono.fromFuture(this.worldEntityIteratorCache.get(world, (key) -> {
                            Reflect worldReflect = Reflect.on(world);
                            Reflect craftWorldReflect = worldReflect
                                    .call("getHandle");
                            System.out.println("CraftWorld: " + craftWorldReflect.get().getClass());
                            System.out.println("CraftWorld fields: " + craftWorldReflect.fields());
                            return craftWorldReflect
                                    .fields()
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> {
                                        Reflect field = entry.getValue();
                                        String fieldName = entry.getKey();
                                        Class<?> type = field.type();
                                        System.out.println(type + " : " + fieldName);
                                        return type
                                                .equals(SpigotReflectionUtil.PERSISTENT_ENTITY_SECTION_MANAGER_CLASS);
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
                        }))
                        .handle((objects, sink) -> {
                            try {
                                objects.getT2().invoke(objects.getT1());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                sink.error(new RuntimeException(e));
                            }
                        })
                        .flatMapMany(nmsEntitiesIterable -> Flux.fromIterable((Iterable<?>) nmsEntitiesIterable)
                                .map(SpigotReflectionUtil::getBukkitEntity))
                        .filter(entity -> entity.getEntityId() == entityId)
                        .singleOrEmpty()));
    }
}
