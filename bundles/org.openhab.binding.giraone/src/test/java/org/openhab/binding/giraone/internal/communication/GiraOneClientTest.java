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
package org.openhab.binding.giraone.internal.communication;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.webservice.GiraOneWebserviceClient;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketClient;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketSequence;

/**
 * Unit Tests for {@link GiraOneClient}.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({})
public class GiraOneClientTest {
    private GiraOneClient giraOneClient;

    private GiraOneClientConfiguration configuration;
    private GiraOneWebsocketClient websocketClient;
    private GiraOneWebserviceClient webserviceClient;
    private GiraOneClientConnectionState connectionState = null;

    @BeforeEach
    void setUp() {
        GiraOneWebsocketSequence.reset();
        configuration = new GiraOneClientConfiguration();
        configuration.username = "User";
        configuration.password = "!Ncc1701D";
        configuration.hostname = "192.168.178.38";
        configuration.maxTextMessageSize = 350000;
        configuration.defaultTimeoutSeconds = 45;
        configuration.discoverButtons = true;

        webserviceClient = Mockito.spy(new GiraOneWebserviceClient(configuration));
        websocketClient = Mockito.spy(new GiraOneWebsocketClient(configuration));
        giraOneClient = new GiraOneClient(configuration, websocketClient, webserviceClient);
        giraOneClient.observeGiraOneConnectionState(giraOneConnectionState -> {
            connectionState = giraOneConnectionState;
        });
    }

    @Test
    @DisplayName("Should connect against gira one server")
    void shouldStartupClient() {
        assertEquals(GiraOneClientConnectionState.Disconnected, connectionState);
        giraOneClient.connect();
        await().atMost(TEN_SECONDS).untilAsserted(() -> {
            assertEquals(GiraOneClientConnectionState.Connected, connectionState);
            assertFalse(giraOneClient.getGiraOneProject().lookupChannels().isEmpty());
        });
    }
}
