package org.openhab.binding.giraone.internal.util;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Generic Builder Pattern taken from https://www.baeldung.com/java-builder-pattern
 *
 * @param <T> The class to build
 */
public class GenericBuilder<T> {
    private final Supplier<T> supplier;

    private GenericBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> GenericBuilder<T> of(Supplier<T> supplier) {
        return new GenericBuilder<>(supplier);
    }

    public <P> GenericBuilder<T> with(BiConsumer<T, P> consumer, P value) {
        return new GenericBuilder<>(() -> {
            T object = supplier.get();
            consumer.accept(object, value);
            return object;
        });
    }

    public T build() {
        return supplier.get();
    }
}