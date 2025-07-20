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

import io.reactivex.rxjava3.disposables.Disposable;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_DATAPOINTS;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_URN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import java.util.List;
import java.util.Map;

/**
 * Test class for {@link GiraOneShutterThingHandler}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneDefaultThingHandlerTest {
    private Thing thing = Mockito.spy(Thing.class);
    private GiraOneDefaultThingHandler handler;
    private GiraOneBridge giraOneBridge;

    private GiraOneChannel findGiraOneChannelWithChannelType(GiraOneChannelType type) {
        return TestDataProvider.createGiraOneProject().lookupChannels().stream().filter(f -> f.getChannelType() == type)
                .findFirst().orElseThrow();
    }

    @BeforeEach
    void setUp() {
        giraOneBridge = mock(GiraOneBridge.class);
        when(giraOneBridge.subscribeOnGiraOneDataPointValues(any(), any())).thenReturn(Disposable.empty());
        when(giraOneBridge.lookupGiraOneProject()).thenReturn(TestDataProvider.createGiraOneProject());

        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        when(thing.getChannels()).thenReturn(List.of());
        when(thing.getThingTypeUID()).thenReturn(GENERIC_TYPE_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of()));

        GiraOneChannel channel = findGiraOneChannelWithChannelType(GiraOneChannelType.Switch);
        when(thing.getProperties()).thenReturn(Map.of(PROPERTY_CHANNEL_URN, channel.getUrn()));

        handler = Mockito.spy(new GiraOneDefaultThingHandler(thing));

        Mockito.doReturn(giraOneBridge).when((GiraOneBaseThingHandler) handler).getGiraOneBridge();

        when(handler.getThing()).thenReturn(thing);
    }

    @DisplayName("Should subscribe on GiraOneChannelValue")
    @Test
    void shouldStartObservingDatapoints() {
        handler.initialize();
        verify(giraOneBridge).subscribeOnConnectionState(any());

        handler.onConnectionState(GiraOneConnectionState.Connected);
        verify(giraOneBridge).subscribeOnGiraOneDataPointValues(any(), any());
    }

    @DisplayName("Should build Thing from GiraOneChannel")
    @Test
    void testBuildThing() {
        GiraOneChannel channel = findGiraOneChannelWithChannelType(GiraOneChannelType.Shutter);
        Thing thing = handler.buildThing(channel);

        assertEquals(channel.getFunctionType().getName(), thing.getProperties().get(PROPERTY_FUNCTION_TYPE));
        assertEquals(channel.getChannelType().getName(), thing.getProperties().get(PROPERTY_CHANNEL_TYPE));
        assertEquals(channel.getChannelTypeId().getName(), thing.getProperties().get(PROPERTY_CHANNEL_TYPE_ID));
        assertEquals(channel.getDataPoints().toString(), thing.getProperties().get(PROPERTY_CHANNEL_DATAPOINTS));
    }
}
