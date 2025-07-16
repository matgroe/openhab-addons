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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;

/**
 * Test class for {@link GiraOneShutterThingHandler}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
class GiraOneTriggerThingHandlerTest {
    private final Thing thing = Mockito.mock(Thing.class);
    private GiraOneTriggerThingHandler handler;
    private GiraOneValue value;

    @BeforeEach
    void setUp() {
        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        value = new GiraOneValue(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback",
                "1");
        handler = Mockito.spy(new GiraOneTriggerThingHandler(thing) {
            @Override
            protected <T> T getConfigAs(Class<T> configurationClass) {
                return (T) new GiraOneClientConfiguration();
            }
        });
        when(handler.getThing()).thenReturn(thing);
        handler.initialize();
        reset(handler);
    }

    @DisplayName("Initial state must be Released")
    @Test
    void testForInitialState() {
        handler.initialize();
        ArgumentCaptor<GiraOneTriggerThingHandler.TriggerState> argumentCaptorState = ArgumentCaptor
                .forClass(GiraOneTriggerThingHandler.TriggerState.class);
        verify(handler, times(1)).updateState(argumentCaptorState.capture());
        assertEquals(1, argumentCaptorState.getAllValues().size());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getFirst());
    }

    @DisplayName("State must move from Released -> Pressed")
    @Test
    void testForStatePressed() {
        handler.initialize();

        ArgumentCaptor<GiraOneTriggerThingHandler.TriggerState> argumentCaptorState = ArgumentCaptor
                .forClass(GiraOneTriggerThingHandler.TriggerState.class);

        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "1",
                "0");
        handler.onGiraOneValue(value);

        verify(handler, times(2)).updateState(argumentCaptorState.capture());

        assertEquals(2, argumentCaptorState.getAllValues().size());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getFirst());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.PRESSED, argumentCaptorState.getAllValues().getLast());
    }

    @DisplayName("State must move from Released -> Pressed -> Hold")
    @Test
    void testForStatePressedToHold() throws Exception {
        handler.initialize();

        ArgumentCaptor<GiraOneTriggerThingHandler.TriggerState> argumentCaptorState = ArgumentCaptor
                .forClass(GiraOneTriggerThingHandler.TriggerState.class);

        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "1",
                "0");
        handler.onGiraOneValue(value);
        Thread.sleep(2000);

        verify(handler, times(3)).updateState(argumentCaptorState.capture());
        assertEquals(3, argumentCaptorState.getAllValues().size());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getFirst());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.PRESSED, argumentCaptorState.getAllValues().get(1));
    }

    @DisplayName("State must move from Released -> Pressed -> Released")
    @Test
    void testForStatePressedToReleased() throws Exception {
        handler.initialize();

        ArgumentCaptor<GiraOneTriggerThingHandler.TriggerState> argumentCaptorState = ArgumentCaptor
                .forClass(GiraOneTriggerThingHandler.TriggerState.class);

        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "1",
                "0");
        handler.onGiraOneValue(value);

        Thread.sleep(800);
        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "0",
                "1");
        handler.onGiraOneValue(value);

        verify(handler, times(3)).updateState(argumentCaptorState.capture());
        assertEquals(3, argumentCaptorState.getAllValues().size());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getFirst());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.PRESSED, argumentCaptorState.getAllValues().get(1));
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getLast());
    }

    @DisplayName("State must move from Released -> Pressed -> Hold -> Released")
    @Test
    void testForStatePressedToHoldToReleased() throws Exception {
        handler.initialize();

        ArgumentCaptor<GiraOneTriggerThingHandler.TriggerState> argumentCaptorState = ArgumentCaptor
                .forClass(GiraOneTriggerThingHandler.TriggerState.class);

        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "1",
                "0");
        handler.onGiraOneValue(value);

        Thread.sleep(2000);
        value = new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Dimming-1:Feedback", "0",
                "1");
        handler.onGiraOneValue(value);

        verify(handler, times(4)).updateState(argumentCaptorState.capture());
        assertEquals(4, argumentCaptorState.getAllValues().size());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getFirst());
        assertEquals(GiraOneTriggerThingHandler.TriggerState.PRESSED, argumentCaptorState.getAllValues().get(1));
        assertEquals(GiraOneTriggerThingHandler.TriggerState.RELEASED, argumentCaptorState.getAllValues().getLast());
    }
}
