package dev.ckateptb.minecraft.jyraf.closable.inject;

import com.google.common.reflect.TypeToken;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.database.repository.Repository;
import dev.ckateptb.minecraft.jyraf.listener.PluginStatusChangeListener;
import org.bukkit.plugin.Plugin;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ClosableInjection implements ComponentRegisterHandler {
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Object object, String qualifier, Plugin owner) {
        if (!(object instanceof AutoCloseable closeable)) return;
        PluginStatusChangeListener.getExecuteOnDisable().computeIfAbsent(owner, key -> ConcurrentHashMap.newKeySet()).add(() -> {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Stream<Tuple2<Type, Type>> resolveEntity(Class<?> repositoryClass) {
        ArrayList<Type> list = new ArrayList<>();
        list.add(repositoryClass.getGenericSuperclass());
        list.addAll(Arrays.asList(repositoryClass.getGenericInterfaces()));
        return list.stream()
                .map(TypeToken::of)
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
