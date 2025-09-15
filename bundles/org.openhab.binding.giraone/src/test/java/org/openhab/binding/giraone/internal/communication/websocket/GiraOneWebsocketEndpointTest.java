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
import static org.awaitility.Duration.ONE_SECOND;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link GiraOneJettyWebsocketEndpoint}
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebsocketEndpointTest {
    private final Logger logger = LoggerFactory.getLogger(GiraOneWebsocketEndpointTest.class);
    private static final String WSS_ECHO_URI = "wss://echo.websocket.org";

    void invokeEndpoint(GiraOneWebsocketEndpoint endpoint) throws Exception {

        endpoint.subscribeOnMessages(x -> {
            logger.info("RCV: {}", x);
        });
        endpoint.subscribeOnConnectionState(state -> {
            logger.info("GiraOneWebsocketConnectionState {}", state);
            if (state == GiraOneWebsocketConnectionState.Connected) {
                endpoint.send("msg-xxxxx");
                for (int i = 0; i < 10; i++) {
                    endpoint.send("msg-xxxxx-" + i);
                    Thread.sleep(200);
                }
            }
        });
        endpoint.connectTo(new URI(WSS_ECHO_URI));

        await().atLeast(ONE_SECOND);
        for (int i = 0; i < 30; i++) {
            logger.info("waiting ....");
            Thread.sleep(500);
        }
        endpoint.disconnect(GiraOneWebsocketCloseCode.NORMAL_CLOSURE);
    }

    @Test
    void doSomeJdkWebsocketThings() throws Exception {
        GiraOneWebsocketEndpoint endpoint = new GiraOneJdkWebsocketEndpoint();
        invokeEndpoint(endpoint);
    }

    // @Test
    // void doSomeOKHttpWebsocketThings() throws Exception {
    // GiraOneWebsocketEndpoint endpoint = new GiraOneOkHttpWebsocketEndpoint();
    // invokeEndpoint(endpoint);
    // }

    @Test
    void doSomeTests() throws Exception {
        GiraOneJettyWebsocketEndpoint endpoint = new GiraOneJettyWebsocketEndpoint();
        invokeEndpoint(endpoint);
    }
}
