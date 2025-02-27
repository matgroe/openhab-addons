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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneClient;
import org.openhab.binding.giraone.internal.communication.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneException;
import org.openhab.binding.giraone.internal.dto.GiraOneChannelDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;
import org.openhab.binding.giraone.internal.dto.GiraOneProjectChannel;
import org.openhab.binding.giraone.internal.dto.GiraOneValue;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * The {@link GiraOneBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneBridgeHandler extends BaseBridgeHandler implements GiraOneBridge {
    private final Logger logger = LoggerFactory.getLogger(GiraOneBridgeHandler.class);
    private final GiraOneClient giraOneServerClient;
    private final Subject<GiraOneChannelDataPoint> channelDataPoints = PublishSubject.create();

    private GiraOneProject giraOneProject = GiraOneProject.empty();

    private Disposable disposableConnectionState = Disposable.empty();
    private Disposable disposableDataPoint = Disposable.empty();

    public GiraOneBridgeHandler(Bridge bridge) {
        super(bridge);
        this.giraOneServerClient = new GiraOneClient(getConfigAs(GiraOneClientConfiguration.class));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(GiraOneThingDiscoveryService.class);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void dispose() {
        disposableConnectionState.dispose();
        disposableDataPoint.dispose();

        disposableConnectionState = Disposable.empty();
        disposableDataPoint = Disposable.empty();

        giraOneProject = GiraOneProject.empty();

        giraOneServerClient.disconnect();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        this.giraOneServerClient.disconnect();
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // intentionally empty, the GiraOne*ThingHandler are responsible for handling commands
    }

    @Override
    public void initialize() {
        logger.info("Initializing 'Gira One Bridge'");

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);

        // background initialization:
        scheduler.execute(() -> {
            try {
                this.giraOneServerClient.connect();
                disposableConnectionState = this.giraOneServerClient
                        .subscribeOnConnectionState(this::onConnectionStateChanged);

                disposableDataPoint = this.giraOneServerClient.subscribeOnGiraOneValues(this::onGiraOneValue);
            } catch (GiraOneException exp) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exp.getMessage());
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void onConnectionStateChanged(GiraOneBridgeConnectionState connectionState) {
        logger.debug("ConnectionStateChanged to {}", connectionState);
        if (connectionState == GiraOneBridgeConnectionState.Connected) {
            lookupGiraOneProject();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    void onGiraOneValue(GiraOneValue giraOneValue) {
        Optional<GiraOneDataPoint> dataPoint = lookupGiraOneProject().lookupGiraOneDataPoint(giraOneValue.getId());
        dataPoint.ifPresent(giraOneDataPoint -> lookupGiraOneProject().lookupGiraOneChannelDataPoints(giraOneDataPoint)
                .stream().peek(x -> x.setValue(giraOneValue.getValue().toString())).forEach(channelDataPoints::onNext));
    }

    @Override
    public synchronized GiraOneProject lookupGiraOneProject() {
        if (this.giraOneProject.lookupChannels().isEmpty()) {
            this.giraOneProject = this.giraOneServerClient.lookupGiraOneProject();
        }
        return this.giraOneProject;
    }

    @Override
    public void lookupGiraOneChannelDataPointValues(final int channelViewId) {
        Optional<GiraOneProjectChannel> channel = this.lookupGiraOneProject()
                .lookupChannelByChannelViewId(channelViewId);
        channel.ifPresent(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .map(GiraOneDataPoint::getId).forEach(giraOneServerClient::lookupGiraOneValue));
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, Object value) {
        this.giraOneServerClient.setGiraOneValue(new GiraOneValue(dataPoint.getId(), value));
    }

    @Override
    public Disposable subscribeOnConnectionState(Consumer<GiraOneBridgeConnectionState> onNextEvent) {
        return this.giraOneServerClient.subscribeOnConnectionState(onNextEvent);
    }

    @Override
    public Disposable subscribeOnGiraOneDataPointStates(final int channelViewId,
            Consumer<GiraOneChannelDataPoint> onNext) {
        return this.channelDataPoints.filter(f -> f.getChannelViewId() == channelViewId).subscribe(onNext);
    }
}
