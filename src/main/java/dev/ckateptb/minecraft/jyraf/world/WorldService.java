package dev.ckateptb.minecraft.jyraf.world;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.joor.Reflect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
public class WorldService {
    private final AsyncCache<World, WorldRepository> worlds = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<Integer, Entity> entityIdCache = Caffeine.newBuilder().buildAsync();
    private final AsyncCache<Integer, Long> entityIdChunkKeyCache = Caffeine.newBuilder().buildAsync();

    private final AsyncCache<World, Supplier<Iterable<?>>> worldEntityIteratorCache = Caffeine.newBuilder().buildAsync();
    private static final CachedReference<Boolean> IS_PAPER_ENTITY_LOOKUP = new CachedReference<>(() -> {
        try {
            Class.forName("io.papermc.paper.chunk.system.entity.EntityLookup");
        } catch (Exception exception) {
            return false;
        }
        return true;
    });

    public void removeEntity(Entity entity) {
        World world = entity.getWorld();
        Optional.ofNullable(worlds.getIfPresent(world)).map(Mono::fromFuture)
                .orElse(Mono.empty()).flatMap(chunkRepository -> chunkRepository.removeEntityFromWorldAndCheckEmpty(entity)
                ).subscribe(empty -> {
                    if (empty) {
                        worlds.asMap().remove(world);
                    }
                    entityIdCache.asMap().remove(entity.getEntityId());
                    entityIdChunkKeyCache.asMap().remove(entity.getEntityId());
                });
    }

    public void storeOrUpdateWithNewChunk(Entity entity, Long newChunkKey) {
        World world = entity.getWorld();
        Mono.fromFuture(this.worlds.get(world, key -> new WorldRepository(key, this)))
                .subscribe(worldRepository -> worldRepository.addOrUpdate(entity, newChunkKey));
    }

    public void storeOrUpdate(Entity entity) {
        World world = entity.getWorld();
        Mono.fromFuture(this.worlds.get(world, key -> new WorldRepository(key, this)))
                .subscribe(worldRepository -> worldRepository.addOrUpdate(entity));
    }

    public Mono<Long> cacheChunkAndGetPreviousIfPresentOrElseCurrent(Entity entity) {
        int entityId = entity.getEntityId();
        CompletableFuture<Long> chunkKey = CompletableFuture.completedFuture(Chunk.getChunkKey(entity.getLocation()));
        CompletableFuture<Long> previous = this.entityIdChunkKeyCache.getIfPresent(entityId);
        if (previous == null) previous = chunkKey;
        this.entityIdChunkKeyCache.put(entityId, chunkKey);
        return Mono.fromFuture(previous);
    }

    public Mono<Entity> getEntityById(World world, int entityId) {
        return Optional.ofNullable(this.entityIdCache.getIfPresent(entityId))
                .map(Mono::fromFuture)
                .orElse(Mono.empty())
                .switchIfEmpty(Mono.defer(() -> Mono.fromFuture(this.worldEntityIteratorCache.get(world, key ->
                                this.getEntityIterable(world)))
                        .<Iterable<?>>handle((supplier, sink) -> {
                            try {
                                sink.next(supplier.get());
                            } catch (Exception e) {
                                sink.error(e);
                            }
                        })
                        .flatMapMany(nmsEntitiesIterable -> Flux.fromIterable(nmsEntitiesIterable)
                                .map(SpigotReflectionUtil::getBukkitEntity)
                                .doOnNext(entity -> this.entityIdCache
                                        .put(entity.getEntityId(), CompletableFuture.completedFuture(entity))))
                        .filter(entity -> entity.getEntityId() == entityId)
                        .singleOrEmpty()));
    }

    private Supplier<Iterable<?>> getEntityIterable(World world) {
        if (IS_PAPER_ENTITY_LOOKUP.get().orElse(false)) return this.getEntityIterablePaper(world);
        return this.getEntityIterablePacketEvents(world);
    }

    private Supplier<Iterable<?>> getEntityIterablePaper(World world) {
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
            return this.getEntityIterablePacketEvents(world);
        }
    }

    private Supplier<Iterable<?>> getEntityIterablePacketEvents(World world) {
        return () -> SpigotReflectionUtil.getEntityList(world);
    }
}
