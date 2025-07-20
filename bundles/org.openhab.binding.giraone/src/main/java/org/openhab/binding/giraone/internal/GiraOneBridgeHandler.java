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
package org.openhab.binding.giraone.internal;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_SERVER_TIME;
import org.openhab.binding.giraone.internal.communication.GiraOneClient;
import org.openhab.binding.giraone.internal.communication.GiraOneClientConnectionState;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneDeviceConfiguration;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The {@link GiraOneBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Groeger - Initial contribution
 */
@Component(service = { GiraOneBridgeHandler.class, GiraOneBridge.class })
@NonNullByDefault
public class GiraOneBridgeHandler extends BaseBridgeHandler implements GiraOneBridge {
    private final static String GDS_DEVICE_CHANNEL_URN = "urn:gds:dp:GiraOneServer.GIOSRVKX03:GDS-Device-Channel";
    private final static String GDS_DEVICE_DATAPOINT_READY = "Ready";
    private final static String GDS_DEVICE_DATAPOINT_LOCAL_TIME = "Local-Time";

    private final static GiraOneDataPoint G1_DATAPOINT_READY = new GiraOneDataPoint(
            GDS_DEVICE_CHANNEL_URN + ":" + GDS_DEVICE_DATAPOINT_READY);

    private final Logger logger = LoggerFactory.getLogger(GiraOneBridgeHandler.class);
    private final GiraOneClient giraOneClient;
    private final CompositeDisposable disposables = new CompositeDisposable();

    /** Observe this subject for Gira Server connection state */
    private final ReplaySubject<GiraOneBridgeState> connectionState = ReplaySubject.createWithSize(1);

    protected final Subject<GiraOneValue> datapointValues = PublishSubject.create();

    public GiraOneBridgeHandler(Bridge bridge) {
        super(bridge);
        this.giraOneClient = new GiraOneClient(getConfigAs(GiraOneClientConfiguration.class));
    }

    protected GiraOneBridgeHandler(Bridge bridge, GiraOneClient giraOneClient) {
        super(bridge);
        this.giraOneClient = giraOneClient;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(GiraOneThingDiscoveryService.class);
    }

    @Override
    public void dispose() {
        disposables.dispose();
        giraOneClient.disconnect();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        this.giraOneClient.disconnect();
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // intentionally empty, the GiraOne*ThingHandler implementations are responsible for handling commands
    }

    @Override
    public void initialize() {
        logger.info("Initializing 'Gira One Bridge Handler'");

        // dispose and clear all disposables
        disposables.clear();

        // Register at GiraOneClient for all Exceptions
        disposables.add(giraOneClient.observeOnGiraOneClientExceptions(this::onGiraOneClientException));

        // Register at GiraOneClient to get all received Values and Events
        disposables.add(giraOneClient.observeGiraOneValues(this::onGiraOneValue));

        // Register for ConnectionState changes
        disposables.add(this.giraOneClient.observeGiraOneConnectionState(this::onConnectionStateChanged));

        subscribeOnGiraOneDataPointValues(String.format("%s:.*", GDS_DEVICE_CHANNEL_URN), this::onDeviceChannelEvent);

        connectionState.onNext(GiraOneBridgeState.Offline);

        scheduler.execute(this::doBackgroundInitialization);
    }

    protected void doBackgroundInitialization() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        this.giraOneClient.disconnect();
        try {
            this.giraOneClient.connect();
        } catch (GiraOneClientException exp) {
            // Note: When initialization can NOT be done set the status with more details for further
            // analysis. See also class ThingStatusDetail for all available status details.
            // Add a description to give user information to understand why thing does not work as expected. E.g.
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
            // "Can not access device as username and/or password are invalid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exp.getMessage());
        }
    }

    private void onGiraOneClientException(GiraOneClientException clientException) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, clientException.getMessage());
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        this.doBackgroundInitialization();
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * Observing function for client connection state changes
     *
     * @param connectionState The {@link GiraOneClient}'s connection state.
     */
    protected void onConnectionStateChanged(GiraOneClientConnectionState connectionState) {
        logger.debug("ConnectionStateChanged to {}", connectionState);
        switch (connectionState) {
            case Connecting -> this.clientMovedToConnecting();
            case Connected -> this.clientMovedToConnected();
            case Disconnected -> this.clientMovedToDisconnected();
            case Error -> this.clientMovedToError();
            case TemporaryUnavailable -> this.clientMovedToTemporaryUnavailable();
        }
    }

    private void onDeviceChannelEvent(GiraOneValue value) {
        logger.trace("onDeviceChannelEvent:: {}", value);
        if (GDS_DEVICE_DATAPOINT_LOCAL_TIME.equalsIgnoreCase(value.getGiraOneDataPoint().getName())) {
            updateState(CHANNEL_SERVER_TIME, DateTimeType.valueOf(value.getValue()));
        } else if (GDS_DEVICE_DATAPOINT_READY.equalsIgnoreCase(value.getGiraOneDataPoint().getName())) {
            if (value.getValueAsBoolean()) {
                this.connectionState.onNext(GiraOneBridgeState.Online);
            } else {
                this.connectionState.onNext(GiraOneBridgeState.Offline);
            }
        }
    }

    private void scheduleReconnect() {
        GiraOneClientConfiguration cfg = getConfigAs(GiraOneClientConfiguration.class);
        logger.info("Schedule server reconnect in {} seconds.", cfg.tryReconnectAfterSeconds);
        scheduler.schedule(this::doBackgroundInitialization, cfg.tryReconnectAfterSeconds, TimeUnit.SECONDS);
    }

    protected void clientMovedToConnecting() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY, "@text/giraone.bridge.try-connect");
    }

    protected void clientMovedToConnected() {
        lookupGiraOneProject();
        this.updateBridgeProperties();
        this.lookupGiraOneDatapointValue(G1_DATAPOINT_READY);
        updateStatus(ThingStatus.ONLINE);
    }

    protected void clientMovedToTemporaryUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/giraone.bridge.temporary-unavailable");
        this.scheduleReconnect();
        this.connectionState.onNext(GiraOneBridgeState.Offline);
    }

    protected void clientMovedToDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        this.connectionState.onNext(GiraOneBridgeState.Offline);
    }

    protected void clientMovedToError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        this.scheduleReconnect();
        this.connectionState.onNext(GiraOneBridgeState.Error);
    }

    void onGiraOneValue(GiraOneValue giraOneValue) {
        logger.debug("onGiraOneValue :: {}", giraOneValue);
        datapointValues.onNext(giraOneValue);
    }

    private void updateBridgeProperties() {
        GiraOneDeviceConfiguration deviceCfg = this.giraOneClient.lookupGiraOneDeviceConfiguration();
        updateProperty("lastUpdate", Date.from(Instant.now()).toString());
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION,
                deviceCfg.get(GiraOneDeviceConfiguration.CURRENT_FIRMWARE_VERSION));
    }

    @Override
    public synchronized GiraOneProject lookupGiraOneProject() {
        return giraOneClient.getGiraOneProject();
    }

    @Override
    public void lookupGiraOneChannelValues(final GiraOneChannel channel) {
        this.logger.debug("lookupGiraOneChannelValues for channel={}", channel);
        channel.getDataPoints().forEach(this::lookupGiraOneDatapointValue);
    }

    @Override
    public void lookupGiraOneDatapointValue(GiraOneDataPoint dataPoint) {
        this.logger.debug("lookupGiraOneDatapointValue for dataPoint={}", dataPoint);
        giraOneClient.lookupGiraOneDatapointValue(dataPoint);
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, String value) {
        this.logger.debug("setGiraOneDataPointValue for dataPoint='{}' to '{}'", dataPoint, value);
        this.giraOneClient.changeGiraOneDataPointValue(dataPoint, value);
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Integer value) {
        this.setGiraOneDataPointValue(dataPoint, value.toString());
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Float value) {
        this.setGiraOneDataPointValue(dataPoint, value.toString());
    }

    @Override
    public Disposable subscribeOnConnectionState(Consumer<GiraOneBridgeState> onNextEvent) {
        return this.connectionState.subscribe(onNextEvent);
    }

    @Override
    public Disposable subscribeOnGiraOneDataPointValues(final String deviceUrnPattern,
            Consumer<GiraOneValue> consumer) {
        return this.datapointValues.filter(f -> f.getDatapointUrn().matches(deviceUrnPattern)).subscribe(consumer);
    }
}
