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
package org.openhab.binding.giraone.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Test class for {@link GiraOneShutterThingHandler}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
class GiraOneShutterThingHandlerTest {
    private Thing thing = Mockito.mock(Thing.class);
    private GiraOneShutterThingHandler handler;

    @BeforeEach
    void setUp() {
        handler = Mockito.spy(new GiraOneShutterThingHandler(thing));
        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        when(handler.getThing()).thenReturn(thing);
    }

    private static Stream<Arguments> provideForTestShutterMovementDetection() {
        return Stream.of(Arguments.of(TestDataProvider.dataPointMovement(), "1", "0", "MOVING"),
                Arguments.of(TestDataProvider.dataPointMovement(), "0", "1", "HALTED"),
                Arguments.of(TestDataProvider.dataPointUpDown(), "10", "20", "MOVING_UP"),
                Arguments.of(TestDataProvider.dataPointUpDown(), "40", "20", "MOVING_DOWN"),
                Arguments.of(TestDataProvider.dataPointPosition(), "10", "20", "MOVING_UP"),
                Arguments.of(TestDataProvider.dataPointPosition(), "10.128", "20.553", "MOVING_UP"),
                Arguments.of(TestDataProvider.dataPointPosition(), "40", "20", "MOVING_DOWN"));
    }

    @DisplayName("Test the shutter movement detection")
    @ParameterizedTest
    @MethodSource("provideForTestShutterMovementDetection")
    void testShutterMovementDetection(GiraOneDataPoint datapoint, String newValue, String oldValue, String expected) {
        GiraOneValueChange valueChange = new GiraOneValueChange(datapoint.getUrn(), newValue, oldValue);
        handler.onGiraOneValue(valueChange);

        ArgumentCaptor<String> argumentCaptorChannel = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<State> argumentCaptorState = ArgumentCaptor.forClass(State.class);
        verify(handler, times(2)).updateState(argumentCaptorChannel.capture(), argumentCaptorState.capture());

        assertEquals(2, argumentCaptorChannel.getAllValues().size());
        assertEquals("motion", argumentCaptorChannel.getAllValues().get(0));
        assertEquals(expected, argumentCaptorState.getAllValues().get(0).toString());

        assertEquals(argumentCaptorChannel.getAllValues().get(1), CaseFormatter.lowerCaseHyphen(datapoint.getName()));
        assertEquals(argumentCaptorState.getAllValues().get(1).toString(), newValue);
    }
}
