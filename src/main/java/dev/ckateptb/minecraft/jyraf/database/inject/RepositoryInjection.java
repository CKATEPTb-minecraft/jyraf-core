package dev.ckateptb.minecraft.jyraf.database.inject;

import com.google.gson.reflect.TypeToken;
import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import dev.ckateptb.minecraft.jyraf.database.repository.Repository;
import org.bukkit.plugin.Plugin;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class RepositoryInjection implements ComponentRegisterCallback {
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Object object, String qualifier, Plugin owner) {
        if (!(object instanceof Repository<?, ?> repository)) return;
        Class<? extends Repository<?, ?>> repositoryClass = (Class<? extends Repository<?, ?>>) repository.getClass();
        Logger logger = owner.getLogger();
        Tuple2<Class<Object>, Class<Object>> tuple2 = this.resolveEntity(repositoryClass)
                .map(tuple -> Tuples.of((Class<Object>) tuple.getT1(), (Class<Object>) tuple.getT2()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.severe("Failed to initialize repository. Make sure you do everything correctly. Example: ");
                    logger.severe("@Component");
                    logger.severe("public class " + repositoryClass.getSimpleName() + " implements [Any]Repository<ENTITY_TYPE_HERE, ENTITY_ID_HERE>");
                    return new RuntimeException();
                });
        Class<Object> entityClass = tuple2.getT1();
        Class<Object> idClass = tuple2.getT2();
        Repository<Object, Object> repo = (Repository<Object, Object>) repository;
        repo.connect(owner, entityClass, idClass);
    }

    private Stream<Tuple2<Type, Type>> resolveEntity(Class<?> repositoryClass) {
        ArrayList<Type> list = new ArrayList<>();
        list.add(repositoryClass.getGenericSuperclass());
        list.addAll(Arrays.asList(repositoryClass.getGenericInterfaces()));
        return list.stream()
                .map(TypeToken::get)
                .filter(typeToken -> Repository.class.isAssignableFrom(typeToken.getRawType()))
                .map(TypeToken::getType)
                .flatMap(type -> {
                    if (type instanceof ParameterizedType parameterizedType) {
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length != 2) return Stream.empty();
                        return Stream.of(Tuples.of(typeArguments[0], typeArguments[1]));
                    } else if (type instanceof Class<?> clazz) {
                        return this.resolveEntity(clazz);
                    } else return Stream.empty();
                });
    }
}
