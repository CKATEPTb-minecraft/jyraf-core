package dev.ckateptb.minecraft.jyraf.database.repository.mongo;

import com.google.gson.JsonObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import dev.ckateptb.minecraft.jyraf.cache.CachedReference;
import dev.ckateptb.minecraft.jyraf.config.serializer.ConfigurationSerializers;
import dev.ckateptb.minecraft.jyraf.database.repository.Repository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNodeFactory;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class MongoRepository<Entity, Id> implements Repository<Entity, Id> {
    private static final CachedReference<GsonConfigurationLoader> MAPPER = new CachedReference<>(() ->
            GsonConfigurationLoader.builder().defaultOptions(ConfigurationOptions.defaults()
                    .shouldCopyDefaults(true)
                    .implicitInitialization(true)
                    .serializers(TypeSerializerCollection.builder()
                            .registerAll(TypeSerializerCollection.defaults())
                            .registerAll(ConfigurationSerializers.getSerializers())
                            .register((type) -> true, ObjectMapper.factory().asTypeSerializer())
                            .build()
                    )
            ).build());
    protected final String url;
    protected final String database;
    private MongoClient client;
    private MongoCollection<Document> collection;
    private CachedReference<MongoCollection<Entity>> dao;
    private String idField;
    private Class<Entity> entityClass;

    @Override
    public void connect(Plugin owner, Class<Entity> entityClass, Class<Id> idClass) {
        if (this.client != null) throw new RuntimeException("Already connected!");
        this.entityClass = entityClass;
        this.client = MongoClients.create(this.url);
        MongoDatabase database = this.client.getDatabase(this.database)
                .withCodecRegistry(
                        CodecRegistries.fromRegistries(
                                MongoClients.getDefaultCodecRegistry(),
                                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
                        )
                );
        DatabaseTable annotation = entityClass.getAnnotation(DatabaseTable.class);
        if (annotation == null) throw new RuntimeException("@DatabaseTable annotation is missing!");
        String name = annotation.tableName();
        this.idField = this.getIdField();
        this.collection = database.getCollection(name);
        this.dao = new CachedReference<>(() -> database.getCollection(name, entityClass));
    }

    private String getIdField() {
        return Arrays.stream(this.entityClass.getDeclaredFields())
                .filter(field -> {
                    if (!field.isAnnotationPresent(DatabaseField.class)) return false;
                    DatabaseField annotation = field.getAnnotation(DatabaseField.class);
                    if (annotation.generatedId()) {
                        throw new RuntimeException("Generated Id is not supported for mongodb");
                    }
                    return annotation.id();
                })
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find the field pointing to the identifier."));
    }

    @Override
    public Mono<Entity> findById(Id id) {
        return Mono.from(this.collection.find(this.adapt(Map.of(idField, id))))
                .map(this::adapt);
    }

    @Override
    public Flux<Entity> findBy(Entity template) {
        return Flux.from(this.collection.find(this.adapt(template)))
                .map(this::adapt);
    }

    @Override
    public Flux<Entity> findBy(Map<String, Object> template) {
        return Flux.from(this.collection.find(this.adapt(template)))
                .map(this::adapt);
    }

    @Override
    public Flux<Entity> findAll() {
        return Flux.from(this.collection.find())
                .map(this::adapt);
    }

    @Override
    public Mono<Entity> save(Entity entity) {
        Document document = this.adapt(entity);
        Object id = document.get(idField);
        if (id == null) throw new NullPointerException(idField + " cant be null");
        return Mono.from(this.collection
                        .replaceOne(Filters.eq(idField, id), document, new ReplaceOptions().upsert(true)))
                .map(updateResult -> {
                    document.put("_id", updateResult.getUpsertedId());
                    return this.adapt(document);
                });
    }

    @Override
    public Mono<Boolean> delete(Id id) {
        return Mono.from(this.collection.deleteOne(Filters.eq(idField, id)))
                .map(deleteResult -> deleteResult.getDeletedCount() > 0);
    }

    @Override
    public Mono<Boolean> exists(Id id) {
        return Mono.from(this.collection.find(Filters.eq(idField, id)))
                .hasElement();
    }

    public MongoCollection<Entity> dao() {
        return this.dao.get().orElse(null);
    }

    public MongoCollection<Document> document() {
        return this.collection;
    }

    @Override
    public void close() {
        if (this.client != null) this.client.close();
    }

    @SneakyThrows
    private Document adapt(Entity entity) {
        Optional<GsonConfigurationLoader> optional = MAPPER.get();
        if (optional.isEmpty()) return new Document();
        GsonConfigurationLoader mapper = optional.get();
        JsonObject json = mapper.createNode().set(entity).get(JsonObject.class);
        if (json == null) return new Document();
        return Document.parse(json.toString());
    }

    @SneakyThrows
    private Document adapt(Map<String, Object> map) {
        Optional<GsonConfigurationLoader> optional = MAPPER.get();
        if (optional.isEmpty()) return new Document();
        GsonConfigurationLoader mapper = optional.get();
        BasicConfigurationNode node = mapper.createNode();
        map.forEach((key, value) -> {
            try {
                node.node(key).set(value);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        });
        JsonObject json = node.get(JsonObject.class);
        if (json == null) return new Document();
        return Document.parse(json.toString());
    }

    @SneakyThrows
    private Entity adapt(Document document) {
        Map<String, Object> map = new HashMap<>(Map.copyOf(document));
        map.remove("_id");
        return MAPPER.get().map(ConfigurationNodeFactory::createNode).map(node -> {
            try {
                return node.set(map).get(this.entityClass);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);
    }
}
