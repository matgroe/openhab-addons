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

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link CaseFormatter}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class CaseFormatterTest {

    private static Stream<Arguments> provideCaseHyphenArguments() {
        return Stream.of(Arguments.of("Hello", "hello"), Arguments.of("HelloWorld", "hello-world"),
                Arguments.of("helloWorld", "hello-world"), Arguments.of("hello-World", "hello-world"));
    }

    @DisplayName("test for correct lower-case-hyphen formatting")
    @ParameterizedTest
    @MethodSource("provideCaseHyphenArguments")
    void testLowerCaseHyphen(String input, String expected) {
        String formatted = CaseFormatter.lowerCaseHyphen(input);
        assertEquals(expected, formatted);
    }
}
