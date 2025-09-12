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

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import nl.altindag.ssl.SSLFactory;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;

/**
 * The handles the raw websocket communication with the Gira One Server
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneJakartaWebsocketEndpoint implements GiraOneWebsocketConnection {

    // websocket close codes : https://www.iana.org/assignments/websocket/websocket.xhtml#close-code-number
    public static final CloseReason WS_CLOSURE_NORMAL = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,
            "Normal Closure");
    public static final CloseReason WS_GOING_AWAY = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Going Away");
    public static final CloseReason WS_CLOSE_ABNORMAL = new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY,
            "Abnormal Closure");

    private final Logger logger = LoggerFactory.getLogger(GiraOneJakartaWebsocketEndpoint.class);

    /** Observe this subject for websocket endpoint connection state */
    // private final ReplaySubject<GiraOneWebsocketConnectionState> endpointConnectionState =
    // ReplaySubject.createWithSize(1);
    private final Subject<GiraOneWebsocketConnectionState> endpointConnectionState = PublishSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Subject<Throwable> queueThrowable = PublishSubject.create();
    private final Subject<String> receiverQueue = PublishSubject.create();
    private final Subject<String> senderQueue = PublishSubject.create();
    private final Subject<CloseReason> websocketCloseReason = PublishSubject.create();

    private Session session = null;

    private class WebsocketEndpoint extends Endpoint {

        private void sendQueuedMessage(Session session, String message) {
            session.getAsyncRemote().sendObject(message);
        }

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
            logger.info("onOpen :: {}", session);
            disposables.add(senderQueue.subscribe(m -> sendQueuedMessage(session, m)));
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    logger.info("onMessage :: {}", message);
                    receiverQueue.onNext(message);
                }
            });
            endpointConnectionState.onNext(GiraOneWebsocketConnectionState.Connected);
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            disposables.clear();
            endpointConnectionState.onNext(GiraOneWebsocketConnectionState.Disconnected);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            logger.error("onError", thr);
            disposables.clear();
            endpointConnectionState.onNext(GiraOneWebsocketConnectionState.Error);
            queueThrowable.onNext(thr);
        }
    }

    public GiraOneJakartaWebsocketEndpoint() {
        this.endpointConnectionState.onNext(GiraOneWebsocketConnectionState.Disconnected);
    }

    @Override
    public void connectTo(URI endpoint) throws GiraOneClientException {
        this.endpointConnectionState.onNext(GiraOneWebsocketConnectionState.Connecting);
        SSLContext sslContext = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build()
                .getSslContext();

        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().sslContext(sslContext).build();

        try {
            session = ContainerProvider.getWebSocketContainer().connectToServer(new WebsocketEndpoint(), cec, endpoint);
        } catch (Exception e) {
            throw new GiraOneClientException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect(CloseReason reason) throws IOException {
        if (this.session != null) {
            this.session.close(reason);
        }
        this.session = null;
    }

    @Override
    public Disposable subscribeOnMessages(Consumer<String> messageConsumer) {
        return this.receiverQueue.subscribe(messageConsumer);
    }

    @Override
    public Disposable subscribeOnConnectionState(Consumer<GiraOneWebsocketConnectionState> stateConsumer) {
        return this.endpointConnectionState.subscribe(stateConsumer);
    }

    @Override
    public Disposable subscribeOnWebsocketCloseReason(Consumer<CloseReason> closeReasonConsumer) {
        return this.websocketCloseReason.subscribe(closeReasonConsumer);
    }

    @Override
    public Disposable subscribeOnThrowable(Consumer<Throwable> throwableConsumer) {
        return this.queueThrowable.subscribe(throwableConsumer);
    }

    @Override
    public void sendMessage(final String message) {
        this.senderQueue.onNext(message);
    }
}
