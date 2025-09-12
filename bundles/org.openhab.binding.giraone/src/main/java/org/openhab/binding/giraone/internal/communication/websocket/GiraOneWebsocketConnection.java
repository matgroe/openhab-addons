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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import jakarta.websocket.CloseReason;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;

import java.io.IOException;
import java.net.URI;

/**
 * The enumeration {@link GiraOneWebsocketConnection} is responsible for describing
 * the current connection state between GiraOneBridge and the physical
 * GiraOne Server within your network.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public interface GiraOneWebsocketConnection {

    void connectTo(URI endpoint) throws GiraOneClientException;

    void disconnect(CloseReason reason) throws IOException;

    void sendMessage(final String message);

    Disposable subscribeOnMessages(Consumer<String> messageConsumer);

    Disposable subscribeOnConnectionState(Consumer<GiraOneWebsocketConnectionState> stateConsumer);

    Disposable subscribeOnWebsocketCloseReason(Consumer<CloseReason> closeReasonConsumer) ;

    Disposable subscribeOnThrowable(Consumer<Throwable> throwableConsumer);

}
