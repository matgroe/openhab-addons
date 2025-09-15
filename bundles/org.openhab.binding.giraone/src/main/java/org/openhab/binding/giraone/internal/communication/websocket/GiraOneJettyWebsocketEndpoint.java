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
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * The handles the raw websocket communication with the Gira One Server
 *
 * @author Matthias Gröger - Initial contribution
 */
class GiraOneJettyWebsocketEndpoint extends GiraOneWebsocketEndpoint implements WebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(GiraOneJettyWebsocketEndpoint.class);

    private final CompositeDisposable disposables = new CompositeDisposable();

    private static final int THREAD_POOL_SIZE = 4;
    private static final int FACTOR_BYTES_2_KILO_BYTES = 1024;
    private static final int DEFAULT_MAX_TEXT_MESSAGE_SIZE = (200 * FACTOR_BYTES_2_KILO_BYTES);
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int maxTextMessageSize = DEFAULT_MAX_TEXT_MESSAGE_SIZE;

    private @Nullable Session websocketSession = null;
    private QueuedThreadPool jettyThreadPool = new QueuedThreadPool(THREAD_POOL_SIZE);

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.warn("onWebSocketBinary :: binyra data is not supported.");
    }

    @Override
    public void onWebSocketText(String message) {
        this.onMessageReceived(message);
    }

    @Override
    public void onWebSocketClose(int code, String reasonPhrase) {
        disposables.clear();
        onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
        onWebsocketClosed(GiraOneWebsocketCloseCode.fromCode(code));
    }

    @Override
    public void onWebSocketConnect(Session session) {
        websocketSession = session;
        disposables.add(subscribeOnSendingQueue(message -> {
            logger.trace("sendWebsocketMessage :: {}", message);
            Objects.requireNonNull(websocketSession).getRemote().sendString(message);
        }));
        onWebsocketState(GiraOneWebsocketConnectionState.Connected);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error("onError", throwable);
        disposables.clear();
        onWebsocketState(GiraOneWebsocketConnectionState.Error);
        onWebsocketError(throwable);
    }

    WebSocketClient createWebSocketClient(HttpClient httpClient) throws GiraOneClientException {
        jettyThreadPool = new QueuedThreadPool(THREAD_POOL_SIZE);
        jettyThreadPool.setName(getClass().getSimpleName());
        jettyThreadPool.setDaemon(true);
        jettyThreadPool.setStopTimeout(0);

        try {
            jettyThreadPool.start();
        } catch (Exception e) {
            throw new GiraOneClientException("Cannot start new ThreadPool.", e);
        }

        WebSocketClient webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.setExecutor(jettyThreadPool);
        webSocketClient.getPolicy().setMaxTextMessageSize(maxTextMessageSize);

        try {
            webSocketClient.start();
            return webSocketClient;
        } catch (Exception e) {
            throw new GiraOneClientException("Cannot start new WebSocketClient.", e);
        }
    }

    public GiraOneJettyWebsocketEndpoint() {
        onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
    }

    @Override
    public void connectTo(URI endpoint) throws GiraOneWebsocketException {
        onWebsocketState(GiraOneWebsocketConnectionState.Connecting);

        try {
            HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
            WebSocketClient webSocketClient = createWebSocketClient(httpClient);
            Future<Session> clientSessionPromise = webSocketClient.connect(this, endpoint);
            clientSessionPromise.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception exp) {
            throw new GiraOneClientException(exp.getMessage(), exp);
        }
    }

    @Override
    public void disconnect(GiraOneWebsocketCloseCode reason) throws GiraOneWebsocketException {
        try {
            if (this.websocketSession != null) {
                Objects.requireNonNull(this.websocketSession).close(reason.getCode(), reason.name());
            }
            this.jettyThreadPool.stop();
        } catch (Exception e) {
            throw new GiraOneWebsocketException(GiraOneClientException.DISCONNECT_FAILED, e);
        } finally {
            this.websocketSession = null;
            onWebsocketState(GiraOneWebsocketConnectionState.Disconnected);
            this.disposables.clear();
        }
    }
}
