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

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_MINUTE;
import static org.awaitility.Duration.ONE_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doReturn;
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
import org.openhab.binding.giraone.internal.communication.GiraOneClientConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.core.library.types.StringType;
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
    private GiraOneBridge bridge = Mockito.mock(GiraOneBridge.class);

    @BeforeEach
    void setUp() {
        handler = Mockito.spy(new GiraOneShutterThingHandler(thing));
        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        when(handler.getThing()).thenReturn(thing);
        doReturn(bridge).when(handler).getGiraOneBridge();
    }

    private static Stream<Arguments> provideForTestShutterMovementDetectionUpDown() {
        return Stream.of(
                Arguments.of(TestDataProvider.dataPointStepUpDown(), "0", "0", GiraOneShutterThingHandler.MotionState.MOVING_UP, GiraOneShutterThingHandler.Direction.UP),
                Arguments.of(TestDataProvider.dataPointStepUpDown(), "1", "0", GiraOneShutterThingHandler.MotionState.MOVING_UP, GiraOneShutterThingHandler.Direction.UP),
                Arguments.of(TestDataProvider.dataPointStepUpDown(), "0", "1", GiraOneShutterThingHandler.MotionState.MOVING_DOWN, GiraOneShutterThingHandler.Direction.DOWN),
                Arguments.of(TestDataProvider.dataPointStepUpDown(), "1", "1", GiraOneShutterThingHandler.MotionState.MOVING_DOWN, GiraOneShutterThingHandler.Direction.DOWN),
                Arguments.of(TestDataProvider.dataPointUpDown(), "0", "0", GiraOneShutterThingHandler.MotionState.MOVING_UP, GiraOneShutterThingHandler.Direction.UP),
                Arguments.of(TestDataProvider.dataPointUpDown(), "1", "0", GiraOneShutterThingHandler.MotionState.MOVING_UP, GiraOneShutterThingHandler.Direction.UP),
                Arguments.of(TestDataProvider.dataPointUpDown(), "0", "1", GiraOneShutterThingHandler.MotionState.MOVING_DOWN, GiraOneShutterThingHandler.Direction.DOWN),
                Arguments.of(TestDataProvider.dataPointUpDown(), "1", "1", GiraOneShutterThingHandler.MotionState.MOVING_DOWN, GiraOneShutterThingHandler.Direction.DOWN)
        );
    }

    @DisplayName("Test the shutter movement detection by channel (step-)up-down)")
    @ParameterizedTest
    @MethodSource("provideForTestShutterMovementDetectionUpDown")
    void testShutterMovementDetectionByStepUpDown(GiraOneDataPoint dataPoint, String oldValue, String newValue, GiraOneShutterThingHandler.MotionState motionState, GiraOneShutterThingHandler.Direction direction) {
        GiraOneValueChange valueChange = new GiraOneValueChange(dataPoint.getUrn(), newValue, oldValue);
        handler.onGiraOneValue(valueChange);

        ArgumentCaptor<String> argumentCaptorChannel = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<State> argumentCaptorState = ArgumentCaptor.forClass(State.class);
        verify(handler, times(2)).updateState(argumentCaptorChannel.capture(), argumentCaptorState.capture());

        assertEquals(2, argumentCaptorChannel.getAllValues().size());
        assertEquals(CaseFormatter.lowerCaseHyphen(dataPoint.getName()), argumentCaptorChannel.getAllValues().get(0));
        assertEquals(StringType.valueOf(direction.toString()), argumentCaptorState.getAllValues().get(0));

        assertEquals("motion-state", argumentCaptorChannel.getAllValues().get(1));
        assertEquals(StringType.valueOf(motionState.toString()), argumentCaptorState.getAllValues().get(1));
    }

    @DisplayName("Test the shutter movement start detection by channel movement")
    @Test
    void testShutterMovementStartDetection() {
        GiraOneValueChange valueChange = new GiraOneValueChange(TestDataProvider.dataPointMovement().getUrn(), "1", "0");
        handler.onGiraOneValue(valueChange);

        ArgumentCaptor<String> argumentCaptorChannel = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<State> argumentCaptorState = ArgumentCaptor.forClass(State.class);
        verify(handler, times(1)).updateState(argumentCaptorChannel.capture(), argumentCaptorState.capture());

        assertEquals(1, argumentCaptorChannel.getAllValues().size());
        assertEquals(GiraOneBindingConstants.CHANNEL_MOTION_STATE, argumentCaptorChannel.getAllValues().get(0));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.MotionState.MOVING.toString()), argumentCaptorState.getAllValues().get(0));

        // Must lookup position
        await().atMost(ONE_SECOND).untilAsserted(() -> {
            ArgumentCaptor<GiraOneDataPoint> argumentCaptorDatapoint = ArgumentCaptor.forClass(GiraOneDataPoint.class);
            verify(bridge, times(1)).lookupGiraOneDatapointValue(argumentCaptorDatapoint.capture());
            assertEquals("Position", argumentCaptorDatapoint.getValue().getName());
        });
    }

    @DisplayName("Test the shutter movement end detection by channel movement")
    @Test
    void testShutterMovementEndDetection() {
        GiraOneValueChange valueChange = new GiraOneValueChange(TestDataProvider.dataPointMovement().getUrn(), "0", "1");
        handler.onGiraOneValue(valueChange);

        ArgumentCaptor<String> argumentCaptorChannel = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<State> argumentCaptorState = ArgumentCaptor.forClass(State.class);
        verify(handler, times(3)).updateState(argumentCaptorChannel.capture(), argumentCaptorState.capture());
        assertEquals(3, argumentCaptorChannel.getAllValues().size());

        assertEquals(GiraOneBindingConstants.CHANNEL_UP_DOWN, argumentCaptorChannel.getAllValues().get(0));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.Direction.UNDEFINED.toString()), argumentCaptorState.getAllValues().get(0));

        assertEquals(GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN, argumentCaptorChannel.getAllValues().get(1));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.Direction.UNDEFINED.toString()), argumentCaptorState.getAllValues().get(1));

        assertEquals(GiraOneBindingConstants.CHANNEL_MOTION_STATE, argumentCaptorChannel.getAllValues().get(2));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.MotionState.HALTED.toString()), argumentCaptorState.getAllValues().get(2));
    }

    @DisplayName("Test the shutter movement detection by channel movement")
    @Test
    void testShutterMovementOnPosition() {
        GiraOneValueChange valueChange = new GiraOneValueChange(TestDataProvider.dataPointPosition().getUrn(), "50", "10");
        handler.onGiraOneValue(valueChange);

        ArgumentCaptor<String> argumentCaptorChannel = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<State> argumentCaptorState = ArgumentCaptor.forClass(State.class);
        verify(handler, times(3)).updateState(argumentCaptorChannel.capture(), argumentCaptorState.capture());

        assertEquals(GiraOneBindingConstants.CHANNEL_MOTION_STATE, argumentCaptorChannel.getAllValues().get(0));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.MotionState.MOVING_DOWN.toString()), argumentCaptorState.getAllValues().get(0));

        assertEquals(GiraOneBindingConstants.CHANNEL_SHUTTER_STATE, argumentCaptorChannel.getAllValues().get(1));
        assertEquals(StringType.valueOf(GiraOneShutterThingHandler.ShutterState.OPEN.toString()), argumentCaptorState.getAllValues().get(1));

        assertEquals(GiraOneBindingConstants.CHANNEL_POSITION, argumentCaptorChannel.getAllValues().get(2));
        assertEquals(StringType.valueOf(valueChange.getValue()), argumentCaptorState.getAllValues().get(2));
    }
}

