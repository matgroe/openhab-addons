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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.communication.GiraOneClient;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Test class for {@link GiraOneBridgeHandler}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class GiraOneBridgeHandlerTest {
    private final Bridge bridge = Mockito.spy(Bridge.class);;
    private GiraOneBridgeHandler bridgeHandler = mock(GiraOneBridgeHandler.class);
    private GiraOneClient giraOneClient = mock(GiraOneClient.class);

    @BeforeEach
    void setUp() {
        giraOneClient = mock(GiraOneClient.class);
        bridgeHandler = spy(new GiraOneBridgeHandler(bridge, giraOneClient));
        when(giraOneClient.observeOnGiraOneClientExceptions(any())).thenReturn(Disposable.empty());
        when(giraOneClient.observeGiraOneValues(any())).thenReturn(Disposable.empty());
        when(giraOneClient.observeGiraOneConnectionState(any())).thenReturn(Disposable.empty());
    }

    @DisplayName("Should start observing GiraOneClient observables and connect")
    @Test
    void testInitialize() throws Exception {
        ArgumentCaptor<Consumer<GiraOneConnectionState>> captorConnectionState = ArgumentCaptor
                .forClass(Consumer.class);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        Field schedulerField = GiraOneBridgeHandler.class.getSuperclass().getSuperclass().getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        schedulerField.set(bridgeHandler, scheduler);

        bridgeHandler.initialize();
        verify(giraOneClient).observeGiraOneValues(any());
        verify(giraOneClient).observeGiraOneConnectionState(captorConnectionState.capture());
        verify(giraOneClient).observeOnGiraOneClientExceptions(any());

        ArgumentCaptor<Runnable> argCaptorRunnable = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).execute(argCaptorRunnable.capture());
        argCaptorRunnable.getValue().run();

        ArgumentCaptor<ThingStatus> argCaptorThingStatus = ArgumentCaptor.forClass(ThingStatus.class);
        ArgumentCaptor<ThingStatusDetail> argCaptorThingStatusDetail = ArgumentCaptor.forClass(ThingStatusDetail.class);

        verify(bridgeHandler, times(1)).updateStatus(argCaptorThingStatus.capture(),
                argCaptorThingStatusDetail.capture(), any());
        assertEquals(ThingStatus.UNKNOWN, argCaptorThingStatus.getValue());
        assertEquals(ThingStatusDetail.BRIDGE_UNINITIALIZED, argCaptorThingStatusDetail.getValue());

        verify(giraOneClient).disconnect();
        verify(giraOneClient).connect();
    }

    private static Stream<Arguments> provideGiraOneValues() {
        return Stream.of(Arguments.of(new GiraOneValueChange(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-5.Switching-2:Feedback",
                "0", "1"), true));
    }

    @DisplayName("Should process received GiraOneValue")
    @ParameterizedTest
    @MethodSource("provideGiraOneValues")
    void shouldDeserialize2WebsocketMessageType(GiraOneValue giraOneValue, boolean shouldProcess) {
        bridgeHandler.onGiraOneValue(giraOneValue);
    }
}
