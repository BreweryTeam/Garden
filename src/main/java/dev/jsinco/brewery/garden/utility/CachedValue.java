package dev.jsinco.brewery.garden.utility;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@NullMarked
public class CachedValue<T> {

    private @Nullable T t = null;
    private final Supplier<T> tSupplier;

    public CachedValue(Supplier<T> tSupplier) {
        this.tSupplier = tSupplier;
    }

    public T get() {
        if (t == null) {
            t = tSupplier.get();
        }
        return t;
    }
}
