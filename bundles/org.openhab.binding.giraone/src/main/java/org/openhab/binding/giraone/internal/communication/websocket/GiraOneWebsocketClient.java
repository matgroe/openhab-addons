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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.MessageTooLargeException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.commands.GetDeviceConfig;
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.commands.GetValue;
import org.openhab.binding.giraone.internal.communication.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.commands.SetValue;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneDeviceConfiguration;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.openhab.core.id.InstanceUUID;
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
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneWebsocketClient implements WebSocketListener {

    private static final String TEMPLATE_WEBSOCKET_URL = "wss://%s:4432/gds/api?%s";
    private static final int FACTOR_BYTES_2_KILO_BYTES = 1024;
    private static final int DEFAULT_MAX_TEXT_MESSAGE_SIZE = (200 * FACTOR_BYTES_2_KILO_BYTES);
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int THREAD_POOL_SIZE = 4;

    // websocket close codes : https://www.iana.org/assignments/websocket/websocket.xhtml#close-code-number
    private static final CloseStatus WS_CLOSURE_NORMAL = new CloseStatus(1000, "Normal Closure");
    private static final CloseStatus WS_GOING_AWAY = new CloseStatus(1001, "Going Away");

    private final Logger logger = LoggerFactory.getLogger(GiraOneWebsocketClient.class);
    private final Gson gson;
    private final String giraOneWssEndpoint;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int maxTextMessageSize = DEFAULT_MAX_TEXT_MESSAGE_SIZE;

    private @Nullable Session websocketSession = null;
    private QueuedThreadPool jettyThreadPool = new QueuedThreadPool(THREAD_POOL_SIZE);

    private Disposable dataPointDisposable = Disposable.empty();

    /** Observe this subject for received Command-Responses from Gira Server */
    final Subject<GiraOneWebsocketResponse> responses = PublishSubject.create();

    /** Observe this subject for received events from Gira Server */
    final Subject<GiraOneEvent> events = PublishSubject.create();

    /** Observe this subject for occuring {@link GiraOneClientException} */
    final Subject<GiraOneClientException> clientExceptions = PublishSubject.create();

    /**
     * Observe this subject for received values from Gira Server. It combines
     * value {@link GiraOneEvent} and {@link GetValue} responses as well.
     */
    final Subject<GiraOneValue> values = PublishSubject.create();

    /** Observe this subject for Gira Server connection state */
    final ReplaySubject<GiraOneConnectionState> connectionState = ReplaySubject.createWithSize(1);

    /**
     * Constructor
     *
     * @param config A {@link GiraOneClientConfiguration} object
     */
    public GiraOneWebsocketClient(final GiraOneClientConfiguration config) {
        Objects.requireNonNull(config.hostname, "GiraOneClientConfiguration 'hostname' must not be null");
        Objects.requireNonNull(config.username, "GiraOneClientConfiguration 'username' must not be null");
        Objects.requireNonNull(config.password, "GiraOneClientConfiguration 'password' must not be null");

        this.gson = GsonMapperFactory.createGson();
        this.giraOneWssEndpoint = String.format(TEMPLATE_WEBSOCKET_URL, config.hostname,
                computeWebsocketAuthToken(config.username, config.password));
        this.connectionState.onNext(GiraOneConnectionState.Disconnected);
        this.timeoutSeconds = config.defaultTimeoutSeconds;
        this.maxTextMessageSize = config.maxTextMessageSize * FACTOR_BYTES_2_KILO_BYTES;
    }

    String computeWebsocketAuthToken(String username, String password) {
        String auth = String.format("%s:%s", username, password);
        return "ui" + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
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

    void initiateWebsocketSession(WebSocketClient webSocketClient) {
        try {
            Future<Session> clientSessionPromise = webSocketClient.connect(this, URI.create(giraOneWssEndpoint));
            clientSessionPromise.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception exp) {
            Throwable cause = exp.getCause() != null ? Objects.requireNonNull(exp.getCause())
                    : Objects.requireNonNull(exp);
            if (exp.getCause() instanceof ConnectException) {
                this.clientExceptions.onNext(new GiraOneClientException(GiraOneClientException.CONNECT_REFUSED, cause));
                this.connectionState.onNext(GiraOneConnectionState.TemporaryUnavailable);
            } else {
                this.clientExceptions
                        .onNext(new GiraOneClientException(GiraOneClientException.CONNECT_CONFIGURATION, cause));
            }
        }
    }

    /**
     * Establish a new Websocket connection to the Gira One Server.
     *
     * @throws GiraOneClientException
     */
    public void connect() {
        if (connectionState.getValue() != GiraOneConnectionState.Disconnected) {
            this.disconnect();
        }

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
        disconnect(WS_CLOSURE_NORMAL);
    }

    private void disconnect(CloseStatus closeStatus) {
        logger.debug("Disconnecting with {}/{}", closeStatus.getCode(), closeStatus.getPhrase());
        try {
            if (this.websocketSession != null) {
                Objects.requireNonNull(this.websocketSession).close(closeStatus);
            }
            this.jettyThreadPool.stop();
        } catch (Exception e) {
            throw new GiraOneClientException(GiraOneClientException.DISCONNECT_FAILED, e);
        } finally {
            this.websocketSession = null;
            this.connectionState.onNext(GiraOneConnectionState.Disconnected);
            this.dataPointDisposable.dispose();
        }
    }

    void observeAndEmitDataPointValues() {
        // dispose existing observable
        dataPointDisposable.dispose();

        // and create a new one
        dataPointDisposable = Observable
                .merge(this.responses.filter(this::isGetValueResponse).map(this::createGiraOneValue),
                        this.events.filter(e -> e.getId() > 0).map(this::createGiraOneValue))
                .retry().subscribe(this.values::onNext, this::onSubscriptionError);
    }

    private boolean isGetValueResponse(GiraOneWebsocketResponse response) {
        return response.getRequestServerCommand().getCommand() instanceof GetValue;
    }

    private void onSubscriptionError(Throwable throwable) {
        logger.error("onSubscriptionError :: {}", throwable.getMessage(), throwable);
    }

    private GiraOneValue createGiraOneValue(GiraOneCommandResponse response) {
        return response.getReply(GiraOneValue.class);
    }

    private GiraOneValue createGiraOneValue(GiraOneEvent event) {
        return new GiraOneValueChange(event.getUrn(), event.getNewValue(), event.getOldValue());
    }

    private void emitConnectionStateException(GiraOneConnectionState expected) {
        this.clientExceptions.onNext(new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                expected.toString(), connectionState.getValue().toString()));
    }

    public GiraOneDeviceConfiguration lookupGiraOneDeviceConfiguration() {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            return execute(GetDeviceConfig.builder().build()).getReply(GiraOneDeviceConfiguration.class);
        }
        throw new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                GiraOneConnectionState.Connected.toString(), connectionState.getValue().toString());
    }

    public GiraOneChannelCollection lookupGiraOneChannels() {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            return execute(GetUIConfiguration.builder().build()).getReply(GiraOneChannelCollection.class);
        }
        throw new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                GiraOneConnectionState.Connected.toString(), connectionState.getValue().toString());
    }

    /**
     * Emits as {@link GetValue} server command to lookup the current value for a datapoint.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to lookup.
     */
    public void lookupGiraOneDataPointValue(final GiraOneDataPoint dataPoint) {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            if (dataPoint.getUrn() != null) {
                send(GetValue.builder().with(GetValue::setUrn, dataPoint.getUrn()).build());
            }
        } else {
            emitConnectionStateException(GiraOneConnectionState.Connected);
        }
    }

    /**
     * Emits as {@link SetValue} server command to change the value for a datapoint.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to lookup.
     * @param value The new value to be set.
     */
    public void changeGiraOneDataPointValue(final GiraOneDataPoint dataPoint, Object value) {
        if (connectionState.getValue() == GiraOneConnectionState.Connected) {
            send(SetValue.builder().with(SetValue::setUrn, dataPoint.getUrn()).with(SetValue::setValue, value).build());
        } else {
            emitConnectionStateException(GiraOneConnectionState.Connected);
        }
    }

    public Disposable subscribeOnConnectionState(Consumer<GiraOneConnectionState> onNext) {
        return this.connectionState.subscribe(onNext);
    }

    public Disposable subscribeOnGiraOneValues(Consumer<GiraOneValue> onNext) {
        return this.values.retry().subscribe(onNext, this::onSubscriptionError);
    }

    public Disposable subscribeOnGiraOneClientExceptions(Consumer<GiraOneClientException> onNext) {
        return this.clientExceptions.subscribe(onNext);
    }

    /**
     * Sends a {@link GiraOneCommand} to Gira One Server. This method
     * is getting used as "fire and forget".
     *
     * @param command The command to send
     */
    void send(GiraOneCommand command) {
        this.send(new GiraOneWebsocketRequest(command));
    }

    /**
     * Sends a {@link GiraOneWebsocketRequest} to Gira One Server. This method
     * is getting used as "fire and forget".
     *
     * @param command The command to send
     */
    void send(GiraOneWebsocketRequest command) {
        try {
            String message = gson.toJson(command, GiraOneWebsocketRequest.class);
            logger.trace("GiraOneWebsocketRequest '{}' :: {}", command.getCommand(), message);
            Objects.requireNonNull(websocketSession).getRemote().sendString(message);
        } catch (IOException exp) {
            throw new GiraOneClientException("Error on executing server command", exp);
        }
    }

    /**
     * Sends a {@link GiraOneCommand} to Gira One Server and waits
     * for the server's command response.
     *
     * @param command The command to send
     */
    GiraOneCommandResponse execute(GiraOneCommand command) {
        return this.execute(new GiraOneWebsocketRequest(command));
    }

    /**
     * Sends a {@link GiraOneWebsocketRequest} to Gira One Server and waits
     * for the server's command response.
     *
     * @param command The command to send
     */
    GiraOneCommandResponse execute(GiraOneWebsocketRequest command) {
        final CompletableFuture<GiraOneWebsocketResponse> promise = new CompletableFuture<>();
        logger.info("Executing GiraOneWebsocketRequest : {}", command.getCommand());
        Disposable disposable = Disposable.empty();
        try {
            disposable = this.responses.filter(f -> f.isInitiatedBy(command)).take(1).subscribe(promise::complete);
            // send out command
            send(command);
            // and wait for response
            return promise.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException exp) {
            throw new GiraOneClientException("Got exception on waiting for command response.", exp);
        } finally {
            disposable.dispose();
        }
    }

    @Override
    @NonNullByDefault({})
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.error("unsupported binary data received");
    }

    @Override
    @NonNullByDefault({})
    public void onWebSocketText(String message) {
        logger.trace("Received Message :: {}", message);
        GiraOneMessageType type = Objects.requireNonNullElse(gson.fromJson(message, GiraOneMessageType.class),
                GiraOneMessageType.Invalid);
        switch (type) {
            case Event -> this.events.onNext(Objects.requireNonNull(gson.fromJson(message, GiraOneEvent.class)));
            case Response ->
                this.responses.onNext(Objects.requireNonNull(gson.fromJson(message, GiraOneWebsocketResponse.class)));
            case Invalid -> this.logger.warn("invalid message received :: {}", message);
            case Error -> this.handleErroneousMessage(
                    Objects.requireNonNull(gson.fromJson(message, GiraOneWebsocketResponse.class)));
        }
    }

    private void handleErroneousMessage(GiraOneWebsocketResponse giraOneCommandResponse) {
        this.logger.error("{} :: {}", giraOneCommandResponse.getGiraMessageError(),
                giraOneCommandResponse.getResponseBody());
    }

    @Override
    @NonNullByDefault({})
    public void onWebSocketClose(int code, String reason) {
        logger.info("WebSocket is closed with code={} and reason={}", code, reason);
        if (code == WS_GOING_AWAY.getCode()) {
            this.connectionState.onNext(GiraOneConnectionState.TemporaryUnavailable);
        }
        this.connectionState.onNext(GiraOneConnectionState.Disconnected);
    }

    @Override
    @NonNullByDefault({})
    public void onWebSocketError(Throwable throwable) {
        logger.error("Received WebSocketError :: ", throwable);
        if (throwable instanceof MessageTooLargeException) {
            this.clientExceptions
                    .onNext(new GiraOneClientException(GiraOneClientException.MESSAGE_TOO_LARGE, throwable));
        } else {
            this.clientExceptions
                    .onNext(new GiraOneClientException(GiraOneClientException.WEBSOCKET_COMMUNICATION, throwable));
        }
        this.connectionState.onNext(GiraOneConnectionState.Error);
    }

    @Override
    @NonNullByDefault({})
    public void onWebSocketConnect(Session session) {
        logger.trace("onWebSocketConnect:: got WebSocket Session :: {}", session);
        this.websocketSession = session;
        this.registerApplication();
    }

    private void registerApplication() {
        CompletableFuture.runAsync(() -> {
            logger.trace("Registering Application");
            execute(RegisterApplication.builder().with(RegisterApplication::setApplicationId, InstanceUUID.get())
                    .with(RegisterApplication::setApplicationType, "api").build());
            this.connectionState.onNext(GiraOneConnectionState.Connected);
        });
    }
}
