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

import static org.junit.jupiter.api.Assertions.*;
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
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Test class for {@link GiraOneShutterThingHandler}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
class GiraOneShutterThingHandlerTest {
    private Thing thing = Mockito.mock(Thing.class);
    private GiraOneShutterThingHandler handler;
    private GiraOneChannelValue channelValue = new GiraOneChannelValue();

    @BeforeEach
    void setUp() {
        handler = Mockito.spy(new GiraOneShutterThingHandler(thing));
        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        when(handler.getThing()).thenReturn(thing);
        channelValue.setChannelViewId(216084);
        channelValue.setChannelViewUrn("urn:gds:chv:Covering-Blind-With-Position-5");
    }

    private static Stream<Arguments> provideForTestShutterMovementDetection() {
        return Stream.of(
                Arguments.of(TestDataProvider.dataPointMovement(), "1", "0",
                        GiraOneShutterThingHandler.MovingState.MOVING),
                Arguments.of(TestDataProvider.dataPointMovement(), "0", "1",
                        GiraOneShutterThingHandler.MovingState.STOPPED),
                Arguments.of(TestDataProvider.dataPointUpDown(), "10", "20",
                        GiraOneShutterThingHandler.MovingState.MOVING_DOWN),
                Arguments.of(TestDataProvider.dataPointUpDown(), "40", "20",
                        GiraOneShutterThingHandler.MovingState.MOVING_UP),
                Arguments.of(TestDataProvider.dataPointPosition(), "10", "20",
                        GiraOneShutterThingHandler.MovingState.MOVING_DOWN),
                Arguments.of(TestDataProvider.dataPointPosition(), "40", "20",
                        GiraOneShutterThingHandler.MovingState.MOVING_UP));
    }

    @DisplayName("Test the shutter movement detection")
    @ParameterizedTest
    @MethodSource("provideForTestShutterMovementDetection")
    void testShutterMovementDetection(GiraOneDataPoint datapoint, String newValue, String oldValue,
            GiraOneShutterThingHandler.MovingState expected) {
        channelValue.setGiraOneDataPoint(datapoint);
        channelValue.setGiraOneValue(new GiraOneValueChange(datapoint.getId(), newValue, oldValue));
        handler.onDataPointState(channelValue);
        assertEquals(handler.movingState, expected);
    }
}
