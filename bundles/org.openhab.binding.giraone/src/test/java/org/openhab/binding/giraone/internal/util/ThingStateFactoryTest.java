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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.giraone.internal.GiraOneBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Test class for {@link ThingStateFactory}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class ThingStateFactoryTest {

    private static Stream<Arguments> provideArguments() {
        return Stream.of(Arguments.of(GiraOneBindingConstants.CHANNEL_ON_OFF, "1", OnOffType.ON),
                Arguments.of(GiraOneBindingConstants.CHANNEL_ON_OFF, "0", OnOffType.OFF),
                Arguments.of(GiraOneBindingConstants.CHANNEL_FLOAT, "10.123", DecimalType.valueOf("10.12")),
                Arguments.of(GiraOneBindingConstants.CHANNEL_FLOAT, "10.128", DecimalType.valueOf("10.13")),
                Arguments.of(GiraOneBindingConstants.CHANNEL_POSITION, "15.99223", DecimalType.valueOf("16")),
                Arguments.of(GiraOneBindingConstants.CHANNEL_SLAT_POSITION, "10.123", DecimalType.valueOf("10"))

        );
    }

    @DisplayName("test for correct lower-case-hyphen formatting")
    @ParameterizedTest
    @MethodSource("provideArguments")
    void test(String channelId, String value, State expected) {
        assertEquals(expected, ThingStateFactory.from(channelId, value));
    }
}
