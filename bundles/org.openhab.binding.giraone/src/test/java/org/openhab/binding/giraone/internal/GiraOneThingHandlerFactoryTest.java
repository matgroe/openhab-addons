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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.BINDING_ID;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Test class for {@link GiraOneBridgeHandler}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class GiraOneThingHandlerFactoryTest {
    private final GiraOneThingHandlerFactory factory = spy(new GiraOneThingHandlerFactory());

    private static Stream<Arguments> provideThingIdentifier() {
        return Stream.of(Arguments.of("shutter-roof-window", GiraOneShutterThingHandler.class),
                Arguments.of("shutter-awning", GiraOneShutterThingHandler.class),
                Arguments.of("shutter-venetian-blind", GiraOneShutterThingHandler.class),
                Arguments.of("function-scene", GiraOneFunctionSceneThingHandler.class),
                Arguments.of("heating-underfloor", GiraOneHeatingUnderfloorThingHandler.class),
                Arguments.of("trigger-button", GiraOneTriggerThingHandler.class),
                Arguments.of("status-humidity", GiraOneStatusThingHandler.class),
                Arguments.of("status-temperature", GiraOneStatusThingHandler.class));
    }

    @DisplayName("should create ThingHandler")
    @ParameterizedTest
    @MethodSource("provideThingIdentifier")
    void testCreateThingHandler(final String thingTypeId, Class<?> expectedClass) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("junit", "thing"));
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(BINDING_ID, thingTypeId));
        assertInstanceOf(expectedClass, factory.createHandler(thing));
    }
}
