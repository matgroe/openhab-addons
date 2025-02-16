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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.giraone.internal.GiraOneBindingConfiguration;
import org.openhab.binding.giraone.internal.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Test class for {@link GiraOneServerClient}
 * 
 * @author Matthias Groeger - Initial contribution
 */
class GiraOneWebsocketClientTest {

    private GiraOneBindingConfiguration configuration;
    private GiraOneServerClient giraClient;

    @BeforeEach
    void setUp() {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ((ch.qos.logback.classic.Logger) root).setLevel(Level.INFO);

        configuration = new GiraOneBindingConfiguration();
        configuration.username = "User";
        configuration.password = "!Ncc1701D";
        configuration.hostname = "192.168.178.38";

        giraClient = new GiraOneServerClient(configuration.hostname, configuration.username, configuration.password);
    }

    @DisplayName("Compute Websocket Authentication Token")
    @ParameterizedTest
    @CsvSource({ "Blah, 1q2w3e4r5t, uiQmxhaDoxcTJ3M2U0cjV0", "User, Pass!Word, uiVXNlcjpQYXNzIVdvcmQ=" })
    public void testComputeWebsocketAuthToken(String username, String password, String expected) {
        String token = giraClient.computeWebsocketAuthToken(username, password);
        assertEquals(expected, token);
    }

    @Test
    void testConnect() throws Exception {
        giraClient.subscribeOnConnectionState(this::xxx);
        giraClient.connect();
        for (int i = 0; i < 100; i++) {
            Thread.sleep(200);
        }
    }

    private void xxx(GiraOneConnectionState connectionState) {
        System.out.println(connectionState);
        if (connectionState == GiraOneConnectionState.Connected) {
            GiraOneProject p = giraClient.lookupGiraOneProject();
            System.out.println(p);
        }
    }
}
