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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import java.net.URI;

/**
 * Test class for {@link GiraOneWebsocketEndpoint}
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebsocketEndpointTest {
    private static final String WSS_ECHO_URI = "wss://echo.websocket.org";

    @Test
    void doSomeTests() throws Exception {
        GiraOneWebsocketEndpoint endpoint = new GiraOneWebsocketEndpoint();
        endpoint.subscribeOnMessages(x -> {
            System.out.println(" xx " + x);
        });
        endpoint.subscribeOnConnectionState(state -> {
            if (state == GiraOneWebsocketConnectionState.Connected) {
                endpoint.sendMessage("xx -> " + System.currentTimeMillis());
            }
        });
        endpoint.connectTo(new URI(WSS_ECHO_URI));

        endpoint.disconnect(GiraOneWebsocketEndpoint.WS_CLOSURE_NORMAL);
        await().atLeast(ONE_SECOND);
    }
}
