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
package org.openhab.binding.giraone.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.GiraOneEvent;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.commands.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;
import org.openhab.binding.giraone.internal.dto.GiraOneProcessView;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;

import com.google.gson.Gson;

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

    private static Stream<Arguments> provideWebsocketMessageTypes() {
        return Stream.of(Arguments.of("/messages/0.Types/001-invalid-message.json", GiraOneMessageType.Invalid),
                Arguments.of("/messages/0.Types/002-response-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/002-response-ok.json", GiraOneMessageType.Response),
                Arguments.of("/messages/0.Types/003-event-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/003-event-ok.json", GiraOneMessageType.Event));
    }

    @DisplayName("message should deserialize to GiraOneMessageType")
    @ParameterizedTest
    @MethodSource("provideWebsocketMessageTypes")
    void shouldDeserialize2WebsocketMessageType(String resourceName, GiraOneMessageType expected) {
        String message = ResourceLoader.loadStringResource(resourceName);
        assertEquals(expected, gson.fromJson(message, GiraOneMessageType.class));
    }

    @Test
    void shouldSerializeObjectOfRegisterApplication() {
        RegisterApplication request = RegisterApplication.builder().build();
        RegisterApplication registerApplication = gson.fromJson(gson.toJson(request, ServerCommand.class),
                RegisterApplication.class);
        assertNotNull(registerApplication);
        assertEquals(request.getApplicationId(), registerApplication.getApplicationId());
        assertEquals(request.getCommand(), registerApplication.getCommand());
    }

    @DisplayName("message should deserialize to GiraOneEvent")
    @Test
    void shouldDeserialize2WebsocketEvent() {
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
        String message = ResourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);
        assertNotNull(response);
        assertEquals(GiraOneCommand.GetUIConfiguration, response.getRequestServerCommand().getCommand());
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse with GiraOneDeviceConfiguration")
    @Test
    void shouldDeserialize2GiraOneCommandResponseWithGiraOneDeviceConfiguration() {
        String message = ResourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);
        assertNotNull(response);
        assertEquals(GiraOneCommand.GetUIConfiguration, response.getRequestServerCommand().getCommand());
        GiraOneProject g1Project = response.getReply(GiraOneProject.class);
        assertNotNull(g1Project);
        g1Project.getAllChannels().forEach(System.out::println);
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse with GiraOneProcessView")
    @Test
    void shouldDeserialize2GiraOneCommandResponseWithGiraOneProcessView() {
        String message = ResourceLoader.loadStringResource("/messages/3.GetProcessView/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);
        assertNotNull(response);
        assertEquals(GiraOneCommand.GetProcessView, response.getRequestServerCommand().getCommand());
        GiraOneProcessView processView = response.getReply(GiraOneProcessView.class);
        assertNotNull(processView);
        processView.getDatapoints().forEach(System.out::println);
    }
}
