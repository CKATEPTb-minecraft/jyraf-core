package dev.ckateptb.minecraft.jyraf.cache;

import java.util.Optional;
import java.util.function.Supplier;

public class CachedReference<T> {
    private T obj;
    private final Supplier<T> supplier;


    public CachedReference() {
        this.supplier = null;
    }

    public CachedReference(T obj) {
        this.supplier = () -> obj;
    }

    public CachedReference(Supplier<T> supplier) {
        this.supplier = supplier;
    }


    public T getIfPresent() {
        return this.obj;
    }

    public Optional<T> get(Supplier<T> supplier) {
        if (this.obj == null && supplier != null) {
            this.obj = supplier.get();
        }
        return Optional.ofNullable(this.obj);
    }

    public void set(T obj) {
        this.obj = obj;
    }

    public Optional<T> get() {
        return this.get(this.supplier);
    }
}
