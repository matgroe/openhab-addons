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
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.util.GenericBuilder;
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
    private final Subject<GiraOneChannelValue> channelValues = PublishSubject.create();

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
        // intentionally empty, the GiraOne*ThingHandler implementations are responsible for handling commands
    }

    @Override
    public void initialize() {
        logger.info("Initializing 'Gira One Bridge Handler'");

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        this.scheduleBackgroundInitialization();
    }

    private void scheduleBackgroundInitialization() {
        scheduler.execute(() -> {
            try {
                this.giraOneServerClient.connect();
                disposableConnectionState = this.giraOneServerClient
                        .subscribeOnConnectionState(this::onConnectionStateChanged);

                disposableDataPoint = this.giraOneServerClient.subscribeOnGiraOneValues(this::onGiraOneValue);
            } catch (GiraOneException exp) {
                // Note: When initialization can NOT be done set the status with more details for further
                // analysis. See also class ThingStatusDetail for all available status details.
                // Add a description to give user information to understand why thing does not work as expected. E.g.
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                // "Can not access device as username and/or password are invalid");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exp.getMessage());
            }
        });
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

    /**
     * Creates a {@link GiraOneChannelValue} from the given {@link GiraOneDataPoint} and
     * {@link GiraOneChannel}
     * objects. Missing data is getting enriched to the destination object, if it's available.
     *
     * @param projectChannel - The {@link GiraOneChannelValue}
     * @param dataPoint - The {@link GiraOneDataPoint}
     *
     * @return the {@link GiraOneChannelValue}
     */
    private GiraOneChannelValue createGiraOneChannelValue(GiraOneChannel projectChannel, GiraOneDataPoint dataPoint) {

        GenericBuilder<GiraOneChannelValue> builder = GenericBuilder.of(GiraOneChannelValue::new);

        return builder.with(GiraOneChannelValue::setChannelViewId, projectChannel.getChannelViewId())
                .with(GiraOneChannelValue::setChannelViewUrn, projectChannel.getChannelViewUrn())
                .with(GiraOneChannelValue::setGiraOneDataPoint, dataPoint).build();
    }

    void onGiraOneValue(GiraOneValue giraOneValue) {
        lookupGiraOneProject().lookupGiraOneDataPoint(giraOneValue.getId())
                .ifPresent(giraOneDataPoint -> lookupGiraOneProject().lookupGiraOneChannels(giraOneDataPoint).stream()
                        .map(channel -> createGiraOneChannelValue(channel, giraOneDataPoint))
                        .peek(c -> c.setGiraOneValue(giraOneValue)).peek(this::write2Log)
                        .forEach(channelValues::onNext));
    }

    private void write2Log(GiraOneChannelValue giraOneChannelValue) {
        this.logger.debug("emitting GiraOneChannelValue :: {}", giraOneChannelValue);
    }

    @Override
    public synchronized GiraOneProject lookupGiraOneProject() {
        if (this.giraOneProject.lookupChannels().isEmpty()) {
            this.giraOneProject = this.giraOneServerClient.lookupGiraOneProject();
        }
        return this.giraOneProject;
    }

    @Override
    public void lookupGiraOneChannelValues(final int channelViewId) {
        Optional<GiraOneChannel> channel = this.lookupGiraOneProject().lookupChannelByChannelViewId(channelViewId);
        channel.ifPresent(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .map(GiraOneDataPoint::getId).forEach(giraOneServerClient::lookupGiraOneValue));
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, String value) {
        this.giraOneServerClient.setGiraOneValue(new GiraOneValue(dataPoint.getId(), value));
    }

    @Override
    public Disposable subscribeOnConnectionState(Consumer<GiraOneBridgeConnectionState> onNextEvent) {
        return this.giraOneServerClient.subscribeOnConnectionState(onNextEvent);
    }

    @Override
    public Disposable subscribeOnGiraOneChannelValue(final int channelViewId, Consumer<GiraOneChannelValue> onNext) {
        return this.channelValues.filter(f -> f.getChannelViewId() == channelViewId).subscribe(onNext);
    }
}
