package dev.jsinco.brewery.garden.utility;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@NullMarked
public sealed interface Lazy<T> extends Supplier<T> {

    static <T> Constant<T> constant(Supplier<T> supplier) {
        return new Lazy.Constant<>(supplier);
    }

    static <T> Variable<T> memorized(Supplier<T> supplier) {
        return new Variable<>(supplier);
    }


    final class Constant<T> implements Lazy<T> {
        private @Nullable Supplier<T> supplier;
        private @Nullable T value;


        private Constant(@Nullable Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (supplier != null) {
                value = supplier.get();
                supplier = null;
            }
            return value;
        }
    }

    final class Variable<T> implements Lazy<T> {
        private @Nullable Supplier<T> supplier;
        private @Nullable T value;

        private Variable(@Nullable Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (value == null) {
                Preconditions.checkState(supplier != null, "Supplier cannot be null when value is null");
                value = supplier.get();
            }
            return value;
        }

        public T reload() {
            value = null;
            return get();
        }
    }
}