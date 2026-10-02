package io.github.apace100.apoli.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private T value;
    private final Supplier<T> getter;

    public Lazy(Supplier<T> getter) {
        this.getter = getter;
    }

    public T get() {
        if (this.value == null) {
            this.value = this.getter.get();
        }

        return this.value;
    }
}
