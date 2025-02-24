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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.giraone.internal.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.commands.GetValue;
import org.openhab.binding.giraone.internal.communication.commands.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;
import org.openhab.binding.giraone.internal.communication.commands.SetValue;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * The class acts as client for the Gira One Server and handles the
 * communication via Websocket.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneClient implements WebSocketListener {
    private final static String TEMPLATE_WEBSOCKET_URL = "wss://%s:4432/gds/api?%s";
    private final static int DEFAULT_MAX_TEXT_MESSAGE_SIZE = (200 * 1024);
    private final static int DEFAULT_TIMEOUT_SECONDS = 10;
    private final static int THREAD_POOL_SIZE = 4;
    private final Logger logger = LoggerFactory.getLogger(GiraOneClient.class);
    private final Gson gson;
    private final String giraOneWssEndpoint;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int maxTextMessageSize = DEFAULT_MAX_TEXT_MESSAGE_SIZE;

    private @Nullable Session websocketSession = null;
    private QueuedThreadPool jettyThreadPool = new QueuedThreadPool(THREAD_POOL_SIZE);

    private Disposable dataPointDisposabe = Disposable.empty();

    /** Observe this subject for received Command-Reponses from Gira Server */
    final Subject<GiraOneCommandResponse> responses = PublishSubject.create();

    /** Observe this subject for received events from Gira Server */
    final Subject<GiraOneEvent> events = PublishSubject.create();

    /**
     * Observe this subject for datapoint values from Gira Server. It combines
     * value events and GetVale responses as well.
     */
    final Subject<GiraOneDataPoint> dataPoints = PublishSubject.create();

    /** Observe this subject for Gira Server connection state */
    final ReplaySubject<GiraOneConnectionState> connectionState = ReplaySubject.createWithSize(1);

    /**
     * Constructor
     *
     * @param host Gira One Server's hostname or IP
     * @param username The username for accessing the Gira functionality
     * @param password The username's password
     */
    public GiraOneClient(final String host, final String username, final String password) {
        Objects.requireNonNull(host, "Argument 'host' must not be null");
        Objects.requireNonNull(username, "Argument 'username' must not be null");
        Objects.requireNonNull(password, "Argument 'password' must not be null");

        this.gson = GsonMapperFactory.createGson();
        this.giraOneWssEndpoint = String.format(TEMPLATE_WEBSOCKET_URL, host,
                computeWebsocketAuthToken(username, password));
        this.connectionState.onNext(GiraOneConnectionState.Disconnected);
    }

    /**
     * Constructor
     *
     * @param config A {@link GiraOneClientConfiguration} object
     */
    public GiraOneClient(final GiraOneClientConfiguration config) {
        this(config.getHostname(), config.getUsername(), config.getPassword());
        this.timeoutSeconds = config.getDefaultTimeoutSeconds();
        this.maxTextMessageSize = config.getMaxTextMessageSize();
    }

    String computeWebsocketAuthToken(String username, String password) {
        String auth = String.format("%s:%s", username, password);
        return "ui" + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
    }

    WebSocketClient createWebSocketClient(HttpClient httpClient) throws GiraOneException {
        jettyThreadPool = new QueuedThreadPool(THREAD_POOL_SIZE);
        jettyThreadPool.setName(getClass().getSimpleName());
        jettyThreadPool.setDaemon(true);
        jettyThreadPool.setStopTimeout(0);

        try {
            jettyThreadPool.start();
        } catch (Exception e) {
            throw new GiraOneException("Cannot start new ThreadPool.", e);
        }

        WebSocketClient webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.setExecutor(jettyThreadPool);
        webSocketClient.getPolicy().setMaxTextMessageSize(maxTextMessageSize);

        try {
            webSocketClient.start();
            return webSocketClient;
        } catch (Exception e) {
            throw new GiraOneException("Cannot start new WebSocketClient.", e);
        }
    }

    void initiateWebsocketSession(WebSocketClient webSocketClient) throws GiraOneException {
        try {
            Future<Session> clientSessionPromise = webSocketClient.connect(this, URI.create(giraOneWssEndpoint));
            clientSessionPromise.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (IOException exp) {
            throw new GiraOneException("Cannot initiate websocket session with " + giraOneWssEndpoint, exp);
        } catch (InterruptedException | TimeoutException | ExecutionException exp) {
            throw new GiraOneException("Cannot resolve client session with given timeout.", exp);
        }
    }

    /**
     * Establish a new Websocket connection to the Gira One Server.
     *
     * @throws GiraOneException
     */
    public void connect() throws GiraOneException {
        logger.debug("Connecting to {}", this.giraOneWssEndpoint);
        this.connectionState.onNext(GiraOneConnectionState.Connecting);

        observeAndEmitDataPointValues();

        HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        WebSocketClient webSocketClient = createWebSocketClient(httpClient);
        initiateWebsocketSession(webSocketClient);
    }

    /**
     * Terminate the websocket connection.
     */
    public void disconnect() {
        try {
            if (this.websocketSession != null) {
                this.websocketSession.close();
            }
            this.jettyThreadPool.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.websocketSession = null;
            this.jettyThreadPool = null;
            this.connectionState.onNext(GiraOneConnectionState.Disconnected);
            this.dataPointDisposabe.dispose();
        }
    }

    void observeAndEmitDataPointValues() {
        dataPointDisposabe = Observable
                .merge(this.responses.filter(f -> f.getRequestServerCommand().getCommand() == GiraOneCommand.GetValue)
                        .map(this::createGiraOneChannelDataPoint), this.events.map(this::createGiraOneChannelDataPoint))
                .subscribe(this.dataPoints::onNext);
    }

    private GiraOneDataPoint createGiraOneChannelDataPoint(GiraOneCommandResponse response) {
        return response.getReply(GiraOneDataPoint.class);
    }

    private GiraOneDataPoint createGiraOneChannelDataPoint(GiraOneEvent event) {
        GiraOneDataPoint dataPoint = new GiraOneDataPoint();
        dataPoint.setId(event.getId());
        dataPoint.setUrn(event.getUrn());
        dataPoint.setValue(event.getNewValue());
        return dataPoint;
    }

    public GiraOneProject lookupGiraOneProject() {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            return execute(GetUIConfiguration.builder().build()).getReply(GiraOneProject.class);
        }
        throw new IllegalStateException("Must be in ConnectionState.Connected");
    }

    public void lookupGiraOneDataPoint(final int datapointId) {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            send(GetValue.builder().with(GetValue::setId, datapointId).build());
        } else {
            throw new IllegalStateException("Must be in ConnectionState.Connected");
        }
    }

    public void setGiraOneDataPointValue(final GiraOneDataPoint datapoint, final String value) {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            send(SetValue.builder().with(SetValue::setId, datapoint.getId()).with(SetValue::setValue, value).build());
        } else {
            throw new IllegalStateException("Must be in ConnectionState.Connected");
        }
    }

    public Disposable subscribeOnConnectionState(Consumer<GiraOneConnectionState> onNext) {
        return this.connectionState.subscribe(onNext);
    }

    public Disposable subscribeOnGiraOneDataPoints(Consumer<GiraOneDataPoint> onNext) {
        return this.dataPoints.subscribe(onNext);
    }

    /**
     * Sends a {@link ServerCommand} to Gira One Server. This method
     * is getting used as "fire and forget".
     *
     * @param command The command to send
     */
    void send(ServerCommand command) {
        try {
            String message = gson.toJson(command, ServerCommand.class);
            logger.debug("ServerCommand '{}' :: {}", command.getCommand(), message);
            Objects.requireNonNull(websocketSession).getRemote().sendString(message);
        } catch (IOException exp) {
            throw new GiraOneException("Error on executing server command", exp);
        }
    }

    /**
     * Sends a {@link ServerCommand} to Gira One Server and waits
     * for the server's command response.
     *
     * @param command The command to send
     */
    GiraOneCommandResponse execute(ServerCommand command) {
        final CompletableFuture<GiraOneCommandResponse> promise = new CompletableFuture<>();
        Disposable disposable = Disposable.empty();
        try {
            disposable = this.responses.filter(f -> f.isInitatedBy(command)).take(1).subscribe(promise::complete);
            // send out command
            send(command);
            // and wait for response
            return promise.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException exp) {
            throw new GiraOneException("Got exception on waiting for command response.", exp);
        } finally {
            disposable.dispose();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.error("Received unsupported binary data from Gira One Server.");
    }

    @Override
    public void onWebSocketText(String message) {
        logger.debug("Received Message :: {}", message);
        GiraOneMessageType type = gson.fromJson(message, GiraOneMessageType.class);
        switch (type) {
            case Event -> this.events.onNext(gson.fromJson(message, GiraOneEvent.class));
            case Response -> this.responses.onNext(gson.fromJson(message, GiraOneCommandResponse.class));
        }
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.debug("WebSocket is closed with code={} and reason={}", code, reason);
        this.connectionState.onNext(GiraOneConnectionState.Disconnected);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error("Received WebSocketError :: ", throwable);
        this.connectionState.onNext(GiraOneConnectionState.Error);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("Received WebSocket Session :: {}", session);
        this.websocketSession = session;
        this.registerApplication();
    }

    private void registerApplication() {
        CompletableFuture.runAsync(() -> {
            execute(RegisterApplication.builder().build());
            this.connectionState.onNext(GiraOneConnectionState.Connected);
        });
    }
}
