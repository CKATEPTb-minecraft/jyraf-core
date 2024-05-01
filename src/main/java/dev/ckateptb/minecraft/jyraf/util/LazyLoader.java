package dev.ckateptb.minecraft.jyraf.util;

import org.apache.commons.lang3.Validate;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LazyLoader<T> {
    protected Supplier<T> supplier;
    private T value;

    private LazyLoader(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyLoader<T> of(Supplier<T> supplier) {
        return new LazyLoader<>(supplier);
    }

    public static <T> Later<T> later() {
        return new Later<>();
    }

    public T get() {
        if (this.value == null) this.value = this.supplier.get();
        return this.value;
    }

    public LazyLoader<T> consume(Consumer<? super T> action) {
        T value = this.get();
        action.accept(value);
        return this;
    }

    public static class Later<T> extends LazyLoader<T> {

        private Later() {
            super(null);
        }

        public void defer(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public boolean isDefined() {
            return this.supplier != null;
        }

        @Override
        public T get() {
            Validate.notNull(this.supplier, "Later, the lazy loader has not yet been initialized. " +
                    "Make sure you call defer.");
            return super.get();
        }
    }
}