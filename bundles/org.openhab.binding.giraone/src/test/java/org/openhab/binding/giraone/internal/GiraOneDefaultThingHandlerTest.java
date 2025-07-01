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
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_DATAPOINTS;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;

/**
 * Test class for {@link GiraOneShutterThingHandler}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneDefaultThingHandlerTest {
    private Thing thing = Mockito.mock(Thing.class);
    private GiraOneDefaultThingHandler handler;

    @BeforeEach
    void setUp() {
        handler = Mockito.spy(new GiraOneDefaultThingHandler(thing));
        when(thing.getUID()).thenReturn(new ThingUID(GENERIC_TYPE_UID, "junit"));
        when(thing.getChannels()).thenReturn(List.of());
        when(thing.getThingTypeUID()).thenReturn(GENERIC_TYPE_UID);
        when(handler.getThing()).thenReturn(thing);
    }

    @Disabled
    void testUpdateThing() {
        GiraOneChannel channel = TestDataProvider.createGiraOneChannel("junit.test.channel-1");
        Thing thing = handler.buildThing(channel);

        assertTrue(thing.getProperties().containsKey(PROPERTY_FUNCTION_TYPE));
        assertTrue(thing.getProperties().containsKey(PROPERTY_CHANNEL_TYPE));
        assertTrue(thing.getProperties().containsKey(PROPERTY_CHANNEL_TYPE_ID));
        assertTrue(thing.getProperties().containsKey(PROPERTY_CHANNEL_DATAPOINTS));

        // assertTrue(capturedArgument.contains("RegisterApplication"),
        // String.format("'%s' must contain 'RegisterApplication'", capturedArgument));
    }
}
