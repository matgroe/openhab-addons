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

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Test class for {@link GiraOneThingDiscoveryService}.
 *
 * @author Matthias Groeger - Initial contribution
 */
class GiraOneThingDiscoveryServiceTest {
    private GiraOneThingDiscoveryService discoveryService = spy(GiraOneThingDiscoveryService.class);;

    @BeforeEach
    void setUp() {
        ThingHandler thingHandler = mock(ThingHandler.class);
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(
                new ThingUID(GiraOneBindingConstants.BINDING_ID, GiraOneBindingConstants.SERVER_TYPE_ID, "junit"));
        when(thingHandler.getThing()).thenReturn(thing);

        discoveryService = spy(GiraOneThingDiscoveryService.class);
        when(discoveryService.getThingHandler()).thenReturn(thingHandler);
        discoveryService.initialize();
    }

    @Test
    void getSupportedThingTypeUIDs() {
    }

    @Test
    void createResult() {
    }

    @Test
    void getThingUID() {
    }
}
