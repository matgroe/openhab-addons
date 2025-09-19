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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.MessageTooLargeException;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneClientConnectionState;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
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
import org.openhab.binding.giraone.internal.types.GiraOneURN;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
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
public class GiraOneWebsocketClient {

    private static final String TEMPLATE_WEBSOCKET_URL = "wss://%s:4432/gds/api?%s";
    private static final int FACTOR_BYTES_2_KILO_BYTES = 1024;
    private static final int DEFAULT_MAX_TEXT_MESSAGE_SIZE = (200 * FACTOR_BYTES_2_KILO_BYTES);
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    private final CompositeDisposable websocketEndpointDisposables = new CompositeDisposable();
    private final Logger logger = LoggerFactory.getLogger(GiraOneWebsocketClient.class);
    private final Gson gson;
    private final String giraOneWssEndpoint;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int maxTextMessageSize = DEFAULT_MAX_TEXT_MESSAGE_SIZE;

    private Disposable dataPointDisposable = Disposable.empty();

    private final GiraOneWebsocketEndpoint websocketConnection;

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
    final ReplaySubject<GiraOneClientConnectionState> connectionState = ReplaySubject.createWithSize(1);

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
        this.connectionState.onNext(GiraOneClientConnectionState.Disconnected);
        this.timeoutSeconds = config.defaultTimeoutSeconds;
        this.maxTextMessageSize = config.maxTextMessageSize * FACTOR_BYTES_2_KILO_BYTES;
        this.websocketConnection = createGiraOneWebsocketConnection();
    }

    String computeWebsocketAuthToken(String username, String password) {
        String auth = String.format("%s:%s", username, password);
        return "ui" + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
    }

    GiraOneWebsocketEndpoint createGiraOneWebsocketConnection() throws GiraOneClientException {
        return new GiraOneJdkWebsocketEndpoint();
    }

    void initiateWebsocketSession() {
        try {
            this.websocketEndpointDisposables.clear();

            // register callbacks
            websocketEndpointDisposables.add(this.websocketConnection.subscribeOnMessages(this::onWebSocketText));
            websocketEndpointDisposables.add(this.websocketConnection.subscribeOnThrowable(this::onWebSocketError));
            websocketEndpointDisposables
                    .add(this.websocketConnection.subscribeOnConnectionState(this::onWebSocketConnectionState));
            websocketEndpointDisposables
                    .add(this.websocketConnection.subscribeOnWebsocketCloseReason(this::onWebSocketClosed));

            this.websocketConnection.connectTo(URI.create(giraOneWssEndpoint));
        } catch (GiraOneClientException exp) {
            this.clientExceptions
                    .onNext(new GiraOneClientException(GiraOneClientException.CONNECT_REFUSED, exp.getCause()));
            this.connectionState.onNext(GiraOneClientConnectionState.TemporaryUnavailable);
        }
    }

    /**
     * Establish a new Websocket connection to the Gira One Server.
     */
    public void connect() {
        if (connectionState.getValue() != GiraOneClientConnectionState.Disconnected) {
            this.disconnect();
        }
        logger.debug("Connecting to {}", this.giraOneWssEndpoint);
        observeAndEmitDataPointValues();
        this.initiateWebsocketSession();
    }

    /**
     * Terminate the websocket connection.
     */
    public void disconnect() {
        this.disconnect(GiraOneWebsocketCloseCode.NORMAL_CLOSURE);
    }

    private void disconnect(GiraOneWebsocketCloseCode closeReason) {
        logger.debug("Disconnecting with {}/{}", closeReason.getCode(), closeReason.toString());
        try {
            this.websocketConnection.disconnect(closeReason);
        } catch (Exception e) {
            throw new GiraOneClientException(GiraOneClientException.DISCONNECT_FAILED, e);
        } finally {
            this.connectionState.onNext(GiraOneClientConnectionState.Disconnected);
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

    private void emitConnectionStateException(GiraOneClientConnectionState expected) {
        this.clientExceptions.onNext(new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                expected.toString(), connectionState.getValue().toString()));
    }

    public GiraOneDeviceConfiguration lookupGiraOneDeviceConfiguration() {
        if (connectionState.getValue() == GiraOneClientConnectionState.Connected) {
            return execute(GetDeviceConfig.builder().build()).getReply(GiraOneDeviceConfiguration.class);
        }
        throw new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                GiraOneClientConnectionState.Connected.toString(),
                Objects.requireNonNull(connectionState.getValue()).toString());
    }

    public GiraOneChannelCollection lookupGiraOneChannels() {
        if (connectionState.getValue() == GiraOneClientConnectionState.Connected) {
            return execute(GetUIConfiguration.builder().build()).getReply(GiraOneChannelCollection.class);
        }
        throw new GiraOneClientException(GiraOneClientException.UNEXPECTED_CONNECTION_STATE,
                GiraOneClientConnectionState.Connected.toString(),
                Objects.requireNonNull(connectionState.getValue()).toString());
    }

    /**
     * Emits as {@link GetValue} server command to lookup the current value for a datapoint.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to lookup.
     */
    public void lookupGiraOneDataPointValue(final GiraOneDataPoint dataPoint) {
        if (connectionState.getValue() == GiraOneClientConnectionState.Connected) {
            if (dataPoint.getUrn() != null && !GiraOneURN.INVALID.equals(dataPoint.getUrn())) {
                send(GetValue.builder().with(GetValue::setUrn, dataPoint.getUrn()).build());
            }
        } else {
            emitConnectionStateException(GiraOneClientConnectionState.Connected);
        }
    }

    /**
     * Emits as {@link SetValue} server command to change the value for a datapoint.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to lookup.
     * @param value The new value to be set.
     */
    public void changeGiraOneDataPointValue(final GiraOneDataPoint dataPoint, Object value) {
        if (connectionState.getValue() == GiraOneClientConnectionState.Connected) {
            send(SetValue.builder().with(SetValue::setUrn, dataPoint.getUrn()).with(SetValue::setValue, value).build());
        } else {
            emitConnectionStateException(GiraOneClientConnectionState.Connected);
        }
    }

    public Disposable subscribeOnConnectionState(Consumer<GiraOneClientConnectionState> onNext) {
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
        String message = gson.toJson(command, GiraOneWebsocketRequest.class);
        logger.trace("GiraOneWebsocketRequest '{}' :: {}", command.getCommand(), message);
        this.websocketConnection.send(message);
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

    @NonNullByDefault({})
    public void onWebSocketClosed(GiraOneWebsocketCloseCode reason) {
        logger.info("WebSocket is closed with code={} and reason={}", reason.getCode(), reason.toString());
        if (reason == GiraOneWebsocketCloseCode.GOING_AWAY || reason == GiraOneWebsocketCloseCode.CLOSED_ABNORMALLY) {
            this.connectionState.onNext(GiraOneClientConnectionState.TemporaryUnavailable);
        }
        this.connectionState.onNext(GiraOneClientConnectionState.Disconnected);
    }

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
        this.connectionState.onNext(GiraOneClientConnectionState.Error);
    }

    public void onWebSocketConnectionState(GiraOneWebsocketConnectionState connectionState) {
        logger.trace("onWebSocketConnectionState:: {}", connectionState);
        if (connectionState == GiraOneWebsocketConnectionState.Connected) {
            this.registerApplication();
        } else {
            this.connectionState.onNext(GiraOneClientConnectionState.valueOf(connectionState.name()));
        }
    }

    private void registerApplication() {
        CompletableFuture.runAsync(() -> {
            logger.trace("Registering Application");
            execute(RegisterApplication.builder()
                    .with(RegisterApplication::setApplicationId, UUID.randomUUID().toString())
                    .with(RegisterApplication::setApplicationType, "api").build());
            this.connectionState.onNext(GiraOneClientConnectionState.Connected);
        });
    }
}
