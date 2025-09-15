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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;
import nl.altindag.ssl.SSLFactory;

/**
 * The handles the raw websocket communication with the Gira One Server
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneJdkWebsocketEndpoint extends GiraOneWebsocketEndpoint {
    private final Logger logger = LoggerFactory.getLogger(GiraOneJdkWebsocketEndpoint.class);
    private WebSocket websocket;
    private HttpClient httpClient;
    private Disposable sendingQueueDisposable = Disposable.empty();

    private class WebSocketClient implements WebSocket.Listener {
        private final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

        private StringBuilder messageBuilder = new StringBuilder();
        private CompletableFuture<?> completedMessageFuture = new CompletableFuture<>();

        @Override
        public void onOpen(WebSocket webSocket) {
            logger.trace("onOpen using subprotocol {}", webSocket.getSubprotocol());
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            CompletableFuture<?> returnValue = completedMessageFuture;
            messageBuilder.append(data);
            webSocket.request(1);

            if (last) {
                logger.trace("message is complete :: {}", messageBuilder.toString());
                onMessageReceived(messageBuilder.toString());
                returnValue.complete(null);
                messageBuilder = new StringBuilder();
                completedMessageFuture = new CompletableFuture<>();
            } else {
                logger.trace("onText received message part:: {}", data);
            }
            return returnValue;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            logger.error("Bad day! {}", webSocket.toString(), error);
            onWebsocketError(error);
        }
    }

    public GiraOneJdkWebsocketEndpoint() {
        this.onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
        this.subscribeOnConnectionState(this::subscribeOnConnectionState);
        this.httpClient = HttpClient.newBuilder().sslContext(createSSLContext()).build();
    }

    private void subscribeOnConnectionState(GiraOneWebsocketConnectionState connectionState) {
        if (connectionState == GiraOneWebsocketConnectionState.Connected) {
            sendingQueueDisposable = this.subscribeOnSendingQueue(this::processSendingQueueItem,
                    this::handleSendingQueueError);
        } else {
            sendingQueueDisposable.dispose();
        }
    }

    private void handleSendingQueueError(Throwable throwable) {
        logger.error("handleSendingQueueError :: {}", throwable.getMessage(), throwable);
        try {
            this.websocket.abort();
            this.httpClient.shutdownNow();
            this.onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
        } catch (Exception e) {
            logger.error("handleSendingQueueError :: exception terminating websocket :: {}", throwable.getMessage(),
                    throwable);
        }
    }

    private void processSendingQueueItem(String item) {
        logger.info("processSendingQueueItem {}", item);
        try {
            this.websocket.sendText(item, true).join().request(1);
        } catch (Exception e) {
            onWebsocketError(e);
        }
    }

    private SSLContext createSSLContext() {
        return SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build().getSslContext();
    }

    @Override
    void connectTo(URI endpoint) throws GiraOneWebsocketException {
        onWebsocketState(GiraOneWebsocketConnectionState.Connecting);
        try {
            this.websocket = this.httpClient.newWebSocketBuilder().buildAsync(endpoint, new WebSocketClient()).get(30,
                    TimeUnit.SECONDS);
            onWebsocketState(GiraOneWebsocketConnectionState.Connected);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new GiraOneWebsocketException(e);
        }
    }

    @Override
    void disconnect(GiraOneWebsocketCloseCode reason) throws GiraOneWebsocketException {
        if (this.websocket != null) {
            this.websocket.sendClose(reason.getCode(), reason.name());
            this.httpClient.shutdownNow();
        }
        this.sendingQueueDisposable.dispose();
        onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
    }
}
