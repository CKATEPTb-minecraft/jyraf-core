package dev.ckateptb.minecraft.jyraf.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class CachedReference<T> {
    @Nullable
    private final Supplier<T> supplier;
    @Nullable
    private T obj;


    public CachedReference() {
        this.supplier = null;
    }

    public CachedReference(T obj) {
        this.supplier = () -> obj;
    }

    public CachedReference(@Nullable Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Nullable
    public T getIfPresent() {
        return this.obj;
    }

    @NotNull
    public Optional<T> get(@Nullable Supplier<T> supplier) {
        if (this.obj == null && supplier != null) {
            this.obj = supplier.get();
        }
        return Optional.ofNullable(this.obj);
    }

    @Nullable
    public T force() {
        return this.get().orElse(null);
    }

    public void set(@Nullable T obj) {
        this.obj = obj;
    }

    @NotNull
    public Optional<T> get() {
        return this.get(this.supplier);
    }
}
