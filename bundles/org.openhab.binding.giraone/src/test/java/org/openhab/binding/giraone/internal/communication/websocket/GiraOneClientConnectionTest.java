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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.util.TestDataProvider;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_MINUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link GiraOneWebsocketClient}
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({})
@Disabled
public class GiraOneClientConnectionTest {
    private GiraOneClientConfiguration configuration = new GiraOneClientConfiguration();
    private GiraOneWebsocketClient giraClient = new GiraOneWebsocketClient(configuration);

    @BeforeEach
    void setUp() {
        configuration.username = "User";
        configuration.password = "!Ncc1701D";
        configuration.hostname = "192.168.178.38";
        configuration.maxTextMessageSize = 350000;
        configuration.defaultTimeoutSeconds = 45;
    }

    @Test
    void testConnectWithInvalidCredentials() {
        configuration.password = "_invalid_";
        giraClient = new GiraOneWebsocketClient(configuration);
        giraClient.connect();
    }

    @Test
    void testConnectWithInvalidHostname() {
        configuration.hostname = "127.0.0.1";
        giraClient = new GiraOneWebsocketClient(configuration);
        giraClient.connect();
    }

    @Test
    void testConnectWithInvalidTextMessageSize() {
        configuration.maxTextMessageSize = 20;
        giraClient = new GiraOneWebsocketClient(configuration);
        giraClient.connect();
    }

    @DisplayName("Test Connect, Register and Disconnect against Gira One Server Websocket")
    @Test
    void testConnectRegisterAndDisconnect() throws Exception {
        GiraOneWebsocketClient giraOneWebsocketClient = new GiraOneWebsocketClient(configuration);

        giraOneWebsocketClient.subscribeOnConnectionState(c -> {
            if (c == GiraOneConnectionState.Connected) {
                // GiraOneDataPoint dp = TestDataProvider.dataPointBuilder("slat-position", 0,
                // "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-4:Slat-Position");
                GiraOneDataPoint dp = TestDataProvider.dataPointBuilder("step-up-down",
                        "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang.Curtain-1:Step-Up-Down");
                // giraOneWebsocketClient.lookupGiraOneValue(dp);
                giraOneWebsocketClient.changeGiraOneDataPointValue(dp, 0);
            }
        });

        giraOneWebsocketClient.connect();

        await().atMost(ONE_MINUTE).untilAsserted(() -> {
            assertEquals(GiraOneConnectionState.Connected, giraOneWebsocketClient.connectionState.getValue());
        });

        Thread.sleep(60000);
    }
}
