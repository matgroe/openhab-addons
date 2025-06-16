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

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.giraone.internal.GiraOneBridgeConnectionState;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.commands.GetDiagnosticDeviceList;

/**
 * Test class for {@link GiraOneWebsocketClient}
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({})
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

    @Test
    void testConnectWithInvalidTextMessageSizeXXX() throws InterruptedException {
        giraClient = new GiraOneWebsocketClient(configuration);
        giraClient.connect();
        await().atMost(TEN_SECONDS).untilAsserted(() -> {
            assertEquals(GiraOneBridgeConnectionState.Connected, giraClient.connectionState.getValue());
            // GiraOneCommandResponse response = giraClient.execute(GetUIConfiguration.builder().build());
            // GiraOneCommandResponse response = giraClient.execute(GetGiraOneDevices.builder().build());
            // GiraOneCommandResponse response = giraClient.execute(GetGiraOneDevices.builder().build());

            GetDiagnosticDeviceList list = GetDiagnosticDeviceList.builder().build();

            // GetConfiguration getCfg = GetConfiguration.builder()
            // .with(GetConfiguration::setId, 206446)
            // .with(GetConfiguration::setUrn,
            // "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang.Curtain-1:Step-Up-Down")
            // .with(GetConfiguration::setUrn,
            // "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang")
            // .with(GetConfiguration::setUrn,
            // "urn:gds:cmp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker3-gang")

            // .build();

            GiraOneCommandResponse response2 = giraClient.execute(list);

            // GiraOneCommandResponse response2 =
            // giraClient.execute(GetConfiguration.builder().with(GetConfiguration::setUrn,
            // "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang").build());



        });

        // for (int i = 0; i < 1000; i++) {
        // System.out.println("sleeping");
        // Thread.sleep(200);
        // }
    }
}
