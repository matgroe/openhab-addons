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
package org.openhab.binding.giraone.internal.typeadapters;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.webservice.GiraOneWebserviceResponse;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketResponse;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneDeviceConfiguration;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for {@link GsonMapperTest}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GsonMapperTest {
    private Gson gson = GsonMapperFactory.createGson();

    @BeforeEach
    void setUp() {
        gson = GsonMapperFactory.createGson();
    }

    private GiraOneCommandResponse createGiraOneWebsocketResponseFrom(final String message) {
        return Objects.requireNonNull(
                gson.fromJson(ResourceLoader.loadStringResource(message), GiraOneWebsocketResponse.class));
    }

    private GiraOneCommandResponse createGiraOneWebserviceResponseFrom(final String message) {
        return Objects.requireNonNull(
                gson.fromJson(ResourceLoader.loadStringResource(message), GiraOneWebserviceResponse.class));
    }

    private static Stream<Arguments> provideWebsocketMessageTypes() {
        return Stream.of(Arguments.of("/messages/0.Types/001-invalid-message.json", GiraOneMessageType.Invalid),
                Arguments.of("/messages/0.Types/002-response-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/002-response-ok.json", GiraOneMessageType.Response),
                Arguments.of("/messages/0.Types/003-event-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/003-event-ok.json", GiraOneMessageType.Event));
    }

    private static Stream<Arguments> provideGiraOneComponentUrns() {
        return Stream.of(
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort4CSystem55Rocker2-gang-10",
                        GiraOneComponentType.KnxButton),
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker3-gang-14",
                        GiraOneComponentType.KnxButton),
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxDimmingActuator6-gang-1",
                        GiraOneComponentType.KnxDimmingActuator),
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxDimmingActuator4-gang-2",
                        GiraOneComponentType.KnxDimmingActuator),
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxHvacActuator12-gang-1",
                        GiraOneComponentType.KnxHvacActuator),
                Arguments.of("urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxHvacActuator6-gang-2",
                        GiraOneComponentType.KnxHvacActuator),
                Arguments.of(
                        "urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator16-gang2C16A2FBlindActuator8-gang-1",
                        GiraOneComponentType.KnxSwitchingActuator),
                Arguments.of(
                        "urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator1-gang-11",
                        GiraOneComponentType.KnxSwitchingActuator),
                Arguments.of(
                        "urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-2",
                        GiraOneComponentType.KnxSwitchingActuator),
                Arguments.of(
                        "urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxUnknownSwitchingActuator6-gang2C16A2FBlindActuator12-gang-2",
                        GiraOneComponentType.Unknown));
    }

    @DisplayName("message should deserialize to GiraOneMessageType")
    @ParameterizedTest
    @MethodSource("provideWebsocketMessageTypes")
    void shouldDeserialize2WebsocketMessageType(String resourceName, GiraOneMessageType expected) {
        String message = ResourceLoader.loadStringResource(resourceName);
        assertEquals(expected, gson.fromJson(message, GiraOneMessageType.class));
    }

    @DisplayName("message should deserialize to GiraOneComponentType")
    @ParameterizedTest
    @MethodSource("provideGiraOneComponentUrns")
    void shouldDeserialize2GiraOneComponentType(String urn, GiraOneComponentType expected) {
        assertEquals(expected, gson.fromJson(new JsonPrimitive(urn), GiraOneComponentType.class));
    }

    @DisplayName("message should deserialize a ValueEvent to GiraOneEvent")
    @Test
    void shouldDeserialize2WebsocketValueEvent() {
        String message = ResourceLoader.loadStringResource("/messages/0.Events/001-evt.json");

        GiraOneEvent event = gson.fromJson(message, GiraOneEvent.class);
        assertNotNull(event);

        assertEquals(220940, event.getId());
        assertEquals("2.3:false", event.getNewInternal());
        assertEquals("2.3:true", event.getOldInternal());
        assertEquals("0", event.getNewValue());
        assertEquals("1", event.getOldValue());
        assertEquals("k:12.0.31", event.getSource());
        assertEquals(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-2:Movement",
                event.getUrn());
        assertEquals("Value", event.getState());
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse")
    @Test
    void shouldDeserialize2GiraOneCommandResponse() {
        GiraOneCommandResponse response = createGiraOneWebsocketResponseFrom(
                "/messages/2.GetUIConfiguration/001-resp.json");
        assertNotNull(response);
        assertNotNull(response.getResponseBody());
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse of GiraOneDeviceConfiguration")
    @Test
    void shouldDeserialize2GiraOneCommandResponseWithGiraOneDeviceConfiguration() {
        GiraOneCommandResponse response = createGiraOneWebsocketResponseFrom(
                "/messages/2.GetUIConfiguration/001-resp.json");
        assertNotNull(response);

        GiraOneChannelCollection channels = response.getReply(GiraOneChannelCollection.class);
        assertNotNull(channels);
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse of GiraOneChannelValue")
    @Test
    void shouldDeserialize2GiraOneCommandResponseWithGiraOneValue() {
        GiraOneCommandResponse response = createGiraOneWebsocketResponseFrom("/messages/2.GetValue/001-resp.json");
        assertNotNull(response);
        GiraOneChannelValue state = response.getReply(GiraOneChannelValue.class);
        assertNotNull(state);
    }

    @DisplayName("message should deserialize to GiraOneEvent")
    @Test
    void shouldDeserialize2GiraOneDeviceConfiguration() {
        GiraOneCommandResponse response = createGiraOneWebsocketResponseFrom(
                "/messages/4.GetDeviceConfig/001-resp.json");

        assertNotNull(response);
        GiraOneDeviceConfiguration deviceCfg = response.getReply(GiraOneDeviceConfiguration.class);
        assertNotNull(deviceCfg);

        assertEquals(deviceCfg.get(GiraOneDeviceConfiguration.CURRENT_APPLICATION_VERSION), "2.0.108.0");
        assertEquals(deviceCfg.get(GiraOneDeviceConfiguration.CURRENT_FIRMWARE_VERSION), "2.0.108.0");
        assertEquals(deviceCfg.get(GiraOneDeviceConfiguration.CURRENT_SYSTEM), "System B");
        assertEquals(deviceCfg.get(GiraOneDeviceConfiguration.DEVICE_ID), "OSRVKX03");
        assertEquals(deviceCfg.get(GiraOneDeviceConfiguration.DEVICE_NAME), "GiraOneServer");
    }

    @DisplayName("GetDiagnosticDeviceList message should deserialize to GiraOneComponents")
    @Test
    void shouldDeserialize2GiraOneComponents() {
        GiraOneCommandResponse response = createGiraOneWebserviceResponseFrom(
                "/messages/9.GetDiagnosticDeviceList/001-resp.json");
        assertNotNull(response);
        response.getReply(GiraOneComponentCollection.class);
    }
}
