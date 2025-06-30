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
package org.openhab.binding.giraone.internal.communication.websocket;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.giraone.internal.GiraOneThingDiscoveryService;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for {@link GiraOneThingDiscoveryService}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({})
class GiraOneWebsocketMessageTest {
    Gson gson;

    @BeforeEach
    void setUp() {
        gson = GsonMapperFactory.createGson();
    }

    @DisplayName("Should deserialize websocket response for GetUIConfiguration")
    @Test
    void shouldSerialzeGetUIConfigurationRequest() {
        GiraOneCommand cmd = GetUIConfiguration.builder().with(GetUIConfiguration::setGuid, "guid")
                .with(GetUIConfiguration::setInstanceId, "instanceId").build();
        GiraOneWebsocketRequest req = new GiraOneWebsocketRequest(cmd);
        String request = gson.toJson(req);

        GiraOneWebsocketRequest req2 = gson.fromJson(request, GiraOneWebsocketRequest.class);
        req2.getCommand();
    }

    @DisplayName("Should deserialize websocket response for GetUIConfiguration")
    @Test
    void shouldDeserializeGetUIConfiguration() {
        String message = ResourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneWebsocketResponse response = gson.fromJson(message, GiraOneWebsocketResponse.class);
        assertNotNull(response);
        assertNotNull(response.responseBody);

        GiraOneChannelCollection uiChannels = response.getReply(GiraOneChannelCollection.class);
        assertNotNull(uiChannels);
        assertFalse(uiChannels.getChannels().isEmpty());
    }

    @Test
    void shouldSerializeObjectOfRegisterApplication() {
        GiraOneWebsocketRequest request = new GiraOneWebsocketRequest(RegisterApplication.builder().build());

        RegisterApplication registerApplication = gson.fromJson(gson.toJson(request, GiraOneWebsocketRequest.class),
                RegisterApplication.class);
        assertNotNull(registerApplication);

        assertInstanceOf(RegisterApplication.class, request.getCommand());
        assertEquals(((RegisterApplication) request.getCommand()).getApplicationId(),
                registerApplication.getApplicationId());
    }
}
