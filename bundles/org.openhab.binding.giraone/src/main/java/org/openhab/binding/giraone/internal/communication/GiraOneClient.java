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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.webservice.GiraOneWebserviceClient;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketClient;
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneDeviceConfiguration;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for interacting with the Gira One Server. It delegates different commands
 * to the concerning websocket or webservice interface.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneClient {
    private final Logger logger = LoggerFactory.getLogger(GiraOneClient.class);

    /** GiraOneClient via websocket API */
    private final GiraOneWebsocketClient websocketClient;

    /** Observe GiraOneClient via webservice API */
    private final GiraOneWebserviceClient webserviceClient;

    /** Observe this subject for Gira Server connection state */
    private final ReplaySubject<GiraOneConnectionState> connectionState = ReplaySubject.createWithSize(1);

    /** Observe this subject for occured {@link GiraOneClientException} */
    private final Subject<GiraOneClientException> clientExceptions = PublishSubject.create();

    private GiraOneProject giraOneProject = new GiraOneProject();

    /**
     * Constructor.
     *
     * @param config The {@link GiraOneClientConfiguration}
     */
    public GiraOneClient(final GiraOneClientConfiguration config) {
        this(new GiraOneWebsocketClient(config), new GiraOneWebserviceClient(config));
    }

    /**
     * Constructor.
     *
     * @param websocketClient The {@link GiraOneWebsocketClient} to use
     * @param webserviceClient The {@link GiraOneWebserviceClient} to use
     */
    public GiraOneClient(GiraOneWebsocketClient websocketClient, GiraOneWebserviceClient webserviceClient) {
        this.websocketClient = websocketClient;
        this.webserviceClient = webserviceClient;

        this.websocketClient.subscribeOnConnectionState(this::onWebsocketConnectionState);
    }

    private void onWebsocketConnectionState(GiraOneConnectionState giraOneConnectionState) {
        if (giraOneConnectionState == GiraOneConnectionState.Connected) {
            this.loadGiraOneProject();
        } else {
            connectionState.onNext(giraOneConnectionState);
        }
    }

    /**
     * Register's a listener for changes on {@link GiraOneConnectionState}.
     *
     * @param consumer The Consumer for {@link GiraOneConnectionState} changes.
     * @return a {@link Disposable}
     */
    public Disposable observeGiraOneConnectionState(Consumer<GiraOneConnectionState> consumer) {
        return connectionState.subscribe(consumer);
    }

    /**
     * Register's a listener for any {@link GiraOneClientException} within on communicating
     * with Gira One Server.
     *
     * @param consumer The Consumer for {@link GiraOneClientException} changes.
     * @return a {@link Disposable}
     */
    public Disposable observeOnGiraOneClientExceptions(Consumer<GiraOneClientException> consumer) {
        return clientExceptions.subscribe(consumer);
    }

    /**
     * Initiates a connection to Gira One Server. The current connection state is reported
     * through the {@link GiraOneConnectionState} observer. Register on
     * {@link GiraOneClient#observeGiraOneConnectionState(Consumer)
     * to get informed about connection state changes.
     *
     */
    public void connect() throws GiraOneClientException {
        logger.trace("Initiating a server connect via webservice");
        try {
            this.connectionState.onNext(GiraOneConnectionState.Connecting);

            this.webserviceClient.connect();
            this.websocketClient.connect();

        } catch (GiraOneCommunicationException commExp) {
            this.connectionState.onNext(GiraOneConnectionState.Error);
            throw new GiraOneClientException(GiraOneClientException.CONNECT_REFUSED, commExp);
        }
    }

    private void loadGiraOneProject() {
        giraOneProject = new GiraOneProject();
        try {
            this.webserviceClient.lookupGiraOneComponentCollection().getAllChannels(GiraOneComponentType.KnxButton)
                    .forEach(giraOneProject::addChannel);
            this.websocketClient.lookupGiraOneChannels().getChannels().forEach(giraOneProject::addChannel);
            this.connectionState.onNext(GiraOneConnectionState.Connected);
        } catch (GiraOneCommunicationException e) {
            this.connectionState.onNext(GiraOneConnectionState.Error);
            giraOneProject = new GiraOneProject();
            this.clientExceptions
                    .onNext(new GiraOneClientException(GiraOneClientException.WEBSERVICE_COMMUNICATION, e));
        }
    }

    public GiraOneProject getGiraOneProject() {
        return this.giraOneProject;
    }

    /**
     * Terminate the connection to Gira One Server.
     */
    public void disconnect() {
        this.websocketClient.disconnect();
    }

    public GiraOneDeviceConfiguration lookupGiraOneDeviceConfiguration() {
        return this.websocketClient.lookupGiraOneDeviceConfiguration();
    }

    /**
     * Initiates the value lookup for the given {@link GiraOneDataPoint}. The determined value
     * will be available through a registered consumer on {@link GiraOneClient#observeGiraOneValues(Consumer)}
     *
     * @param dataPoint The Datapoint to lookup it's value.
     */
    public void lookupGiraOneDatapointValue(GiraOneDataPoint dataPoint) {
        this.websocketClient.lookupGiraOneDataPointValue(dataPoint);
    }

    /**
     * Changes the value for a {@link GiraOneDataPoint}.
     *
     * @param dataPoint The {@link GiraOneDataPoint} to change.
     * @param newValue The new value.
     */
    public void changeGiraOneDataPointValue(GiraOneDataPoint dataPoint, String newValue) {
        this.websocketClient.changeGiraOneDataPointValue(dataPoint, newValue);
    }

    /**
     * Register's a listener for {@link GiraOneValue}. A value is getting reported
     * on receiving an event from gira one server. This may be initiated by invoking
     * {@link #lookupGiraOneDatapointValue(GiraOneDataPoint)} or by any value event
     * as received from the Gira One Server.
     *
     * @param consumer The Consumer for {@link GiraOneValue} changes.
     * @return a {@link Disposable}
     */
    public Disposable observeGiraOneValues(Consumer<GiraOneValue> consumer) {
        return this.websocketClient.subscribeOnGiraOneValues(consumer);
    }
}
