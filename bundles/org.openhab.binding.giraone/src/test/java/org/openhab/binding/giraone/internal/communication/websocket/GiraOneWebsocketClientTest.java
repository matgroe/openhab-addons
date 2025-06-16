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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.GiraOneBridgeConnectionState;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.commands.GetDeviceConfig;
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.commands.GetValue;
import org.openhab.binding.giraone.internal.communication.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

/**
 * Test class for {@link GiraOneWebsocketClient}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
class GiraOneWebsocketClientTest {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final int RCV_TIMEOUT = 2;

    private GiraOneWebsocketClient giraOneWebsocketClient;
    private Session websocketSession = Mockito.mock(Session.class);
    private RemoteEndpoint remoteEndpoint = mock(RemoteEndpoint.class);

    @BeforeEach
    void setUp() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        GiraOneWebsocketSequence.reset();

        GiraOneClientConfiguration configuration = new GiraOneClientConfiguration();
        configuration.username = "User";
        configuration.password = "secret";
        configuration.hostname = "127.0.0.1";
        configuration.maxTextMessageSize = 350000;
        configuration.defaultTimeoutSeconds = 45;

        websocketSession = Mockito.mock(Session.class);
        remoteEndpoint = mock(RemoteEndpoint.class);

        when(websocketSession.getRemote()).thenReturn(remoteEndpoint);

        Future<Session> session = mock(Future.class);
        when(session.get(configuration.defaultTimeoutSeconds, TimeUnit.SECONDS)).thenReturn(this.websocketSession);

        WebSocketClient webSocketClient = Mockito.mock(WebSocketClient.class);
        Mockito.when(webSocketClient.connect(any(GiraOneWebsocketClient.class), any(URI.class))).thenReturn(session);

        giraOneWebsocketClient = Mockito.spy(new GiraOneWebsocketClient(configuration));
        Mockito.doReturn(webSocketClient).when(giraOneWebsocketClient)
                .createWebSocketClient(Mockito.any(HttpClient.class));
    }

    @DisplayName("Compute Websocket Authentication Token")
    @ParameterizedTest
    @CsvSource({ "Blah, 1q2w3e4r5t, uiQmxhaDoxcTJ3M2U0cjV0", "User, Pass!Word, uiVXNlcjpQYXNzIVdvcmQ=" })
    public void testComputeWebsocketAuthToken(String username, String password, String expected) {
        String token = giraOneWebsocketClient.computeWebsocketAuthToken(username, password);
        assertEquals(expected, token);
    }

    @DisplayName("Test Connect, Register and Disconnect against Gira One Server Websocket")
    @Test
    void testConnectRegisterAndDisconnect() throws Exception {
        assertEquals(GiraOneBridgeConnectionState.Disconnected, giraOneWebsocketClient.connectionState.getValue());

        giraOneWebsocketClient.connect();
        assertEquals(GiraOneBridgeConnectionState.Connecting, giraOneWebsocketClient.connectionState.getValue());

        giraOneWebsocketClient.onWebSocketConnect(websocketSession);

        await().atMost(ONE_SECOND).untilAsserted(() -> {
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(remoteEndpoint).sendString(argumentCaptor.capture());
            String capturedArgument = argumentCaptor.getValue();
            assertTrue(capturedArgument.contains("RegisterApplication"),
                    String.format("'%s' must contain 'RegisterApplication'", capturedArgument));
        });

        giraOneWebsocketClient
                .onWebSocketText(ResourceLoader.loadStringResource("/messages/1.RegisterApplication/001-resp.json"));
        await().atMost(ONE_SECOND).untilAsserted(() -> {
            assertEquals(GiraOneBridgeConnectionState.Connected, giraOneWebsocketClient.connectionState.getValue());
        });

        giraOneWebsocketClient.disconnect();
        assertEquals(GiraOneBridgeConnectionState.Disconnected, giraOneWebsocketClient.connectionState.getValue());
    }

    private void sendWebsocketText(final String text) {
        executorService.schedule(() -> {
            giraOneWebsocketClient.onWebSocketText(text);
        }, 1, TimeUnit.SECONDS);
    }

    private static Stream<Arguments> provideServerCommandMessages() {
        return Stream.of(Arguments.of("/messages/1.RegisterApplication/001-resp.json", RegisterApplication.class),
                Arguments.of("/messages/2.GetUIConfiguration/001-resp.json", GetUIConfiguration.class),
                Arguments.of("/messages/4.GetDeviceConfig/001-resp.json", GetDeviceConfig.class),
                Arguments.of("/messages/2.GetValue/001-resp.json", GetValue.class));
    }

    @DisplayName("Received ServerCommandResponses must be provided by 'responses' Observable ")
    @ParameterizedTest
    @MethodSource("provideServerCommandMessages")
    void testOnWebSocketTextWithServerCommandResponse(String messageSource, Class<GiraOneCommand> command) {
        sendWebsocketText(ResourceLoader.loadStringResource(messageSource));
        GiraOneCommandResponse response = giraOneWebsocketClient.responses.firstElement()
                .timeout(RCV_TIMEOUT, TimeUnit.SECONDS).blockingGet();
        assertNotNull(response);

    }

    @DisplayName("Received GiraEvents must be provided by 'events' Observable ")
    @Test
    void testOnWebSocketText4ValueEvent() {
        sendWebsocketText(ResourceLoader.loadStringResource("/messages/0.Events/001-evt.json"));
        GiraOneEvent event = giraOneWebsocketClient.events.firstElement().timeout(RCV_TIMEOUT, TimeUnit.SECONDS)
                .blockingGet();
        assertNotNull(event);
    }

    @DisplayName("Received GiraEvents must be mapped to DataPoint and provided by 'values' Observable ")
    @Test
    void shoutEmitDatapointOnWebSocketTextWithValueEvent() {
        giraOneWebsocketClient.observeAndEmitDataPointValues();
        sendWebsocketText(ResourceLoader.loadStringResource("/messages/0.Events/001-evt.json"));
        GiraOneValue dp = giraOneWebsocketClient.values.firstElement().timeout(RCV_TIMEOUT, TimeUnit.SECONDS)
                .blockingGet();
        assertNotNull(dp);
        assertInstanceOf(GiraOneValueChange.class, dp);
        assertEquals("1", ((GiraOneValueChange) dp).getPreviousValue());
        assertEquals("0", dp.getValue());
    }

    @DisplayName("Received GetValue responses must be mapped to DataPoint and provided by 'dataPoints' Observable ")
    @Test
    void shoutEmitDatapointOnWebSocketTextWithValueResponse() {
        giraOneWebsocketClient.observeAndEmitDataPointValues();
        sendWebsocketText(ResourceLoader.loadStringResource("/messages/2.GetValue/001-resp.json"));
        GiraOneValue dp = giraOneWebsocketClient.values.firstElement().timeout(RCV_TIMEOUT, TimeUnit.SECONDS)
                .blockingGet();
        assertNotNull(dp);
        assertEquals(1002, dp.getId());
        assertEquals("1", dp.getValue());
    }

    @DisplayName("Received ServerCommandResponses must not be provided by 'dataPoints' Observable ")
    @ParameterizedTest
    @ValueSource(strings = { "/messages/1.RegisterApplication/001-resp.json",
            "/messages/2.GetUIConfiguration/001-resp.json", "/messages/3.GetProcessView/001-resp.json",
            "/messages/4.GetDeviceConfig/001-resp.json", "/messages/5.GetConfiguration/001-resp.json",
            "/messages/6.GetNextTriggerTimes/001-resp.json" })
    void shouldNotEmitDatapointOnWebSocketTextWithCommandResponse(String messageSource) {
        giraOneWebsocketClient.observeAndEmitDataPointValues();
        sendWebsocketText(ResourceLoader.loadStringResource(messageSource));
        GiraOneValue dp = giraOneWebsocketClient.values.firstElement().timeout(RCV_TIMEOUT, TimeUnit.SECONDS)
                .onErrorComplete().blockingGet();
        assertNull(dp);
    }
}
