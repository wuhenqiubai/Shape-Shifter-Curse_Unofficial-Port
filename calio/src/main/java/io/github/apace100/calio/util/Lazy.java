package io.github.apace100.calio.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private T cached;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (this.cached == null) {
            this.cached = this.supplier.get();
        }

        return this.cached;
    }
}
