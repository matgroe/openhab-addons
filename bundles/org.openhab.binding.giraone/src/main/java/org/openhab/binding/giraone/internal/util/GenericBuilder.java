/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.giraone.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Generic Builder Pattern taken from https://www.baeldung.com/java-builder-pattern
 *
 * @param <T> The class to build
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
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
