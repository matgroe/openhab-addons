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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneClient;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneDeviceConfiguration;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.util.GenericBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
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
    private final Logger logger = LoggerFactory.getLogger(GiraOneBridgeHandler.class);
    private final GiraOneClient giraOneClient;
    private final Subject<GiraOneChannelValue> channelValues = PublishSubject.create();

    private Disposable disposableConnectionState = Disposable.empty();
    private Disposable disposableDataPoint = Disposable.empty();

    public GiraOneBridgeHandler(Bridge bridge) {
        super(bridge);
        this.giraOneClient = new GiraOneClient(getConfigAs(GiraOneClientConfiguration.class));
        giraOneClient.observeOnGiraOneClientExceptions(this::onGiraOneClientException);
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
        scheduler.execute(this::doBackgroundInitialization);
    }

    private void doBackgroundInitialization() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        this.giraOneClient.disconnect();
        try {
            disposableConnectionState = this.giraOneClient
                    .observeGiraOneConnectionState(this::onConnectionStateChanged);
            disposableDataPoint = this.giraOneClient.observeGiraOneValues(this::onGiraOneValue);

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

    /**
     *
     * @param connectionState The {@link GiraOneClient}'s connection state.
     */
    private void onConnectionStateChanged(GiraOneConnectionState connectionState) {
        logger.debug("ConnectionStateChanged to {}", connectionState);
        switch (connectionState) {
            case Connecting -> this.clientMovedToConnecting();
            case Connected -> this.clientMovedToConnected();
            case Disconnected -> this.clientMovedToDisconnected();
            case Error -> this.clientMovedToError();
            case TemporaryUnavailable -> this.clientMovedToTemporaryUnavailable();
        }
    }

    protected void clientMovedToConnecting() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY, "@text/bridge.try-connect");
    }

    protected void clientMovedToConnected() {
        lookupGiraOneProject();
        this.lookupGiraOneConfiguration();
        updateStatus(ThingStatus.ONLINE);
    }

    protected void clientMovedToTemporaryUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/bridge.temporary-unavailable");
        GiraOneClientConfiguration cfg = getConfigAs(GiraOneClientConfiguration.class);
        scheduler.schedule(this::doBackgroundInitialization, cfg.tryReconnectAfterSeconds, TimeUnit.SECONDS);
    }

    protected void clientMovedToDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
    }

    protected void clientMovedToError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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

        return builder.with(GiraOneChannelValue::setChannelViewUrn, projectChannel.getUrn())
                .with(GiraOneChannelValue::setGiraOneDataPoint, dataPoint).build();
    }

    void onGiraOneValue(GiraOneValue giraOneValue) {
        lookupGiraOneProject().lookupGiraOneDataPoint(giraOneValue.getUrn())
                .ifPresent(giraOneDataPoint -> lookupGiraOneProject().lookupGiraOneChannels(giraOneDataPoint).stream()
                        .map(channel -> createGiraOneChannelValue(channel, giraOneDataPoint))
                        .peek(c -> c.setGiraOneValue(giraOneValue)).peek(this::write2Log)
                        .forEach(channelValues::onNext));
    }

    private void write2Log(GiraOneChannelValue giraOneChannelValue) {
        this.logger.trace("emitting GiraOneChannelValue :: {}", giraOneChannelValue);
    }

    private void lookupGiraOneConfiguration() {
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
        this.logger.trace("lookupGiraOneChannelValues for channel={}", channel);
        channel.getDataPoints().forEach(giraOneClient::lookupGiraOneDatapointValue);
    }

    @Override
    public void setGiraOneDataPointValue(GiraOneDataPoint dataPoint, String value) {
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
    public Disposable subscribeOnConnectionState(Consumer<GiraOneConnectionState> onNextEvent) {
        return this.giraOneClient.observeGiraOneConnectionState(onNextEvent);
    }

    @Override
    public Disposable subscribeOnGiraOneChannelValue(final GiraOneChannel channel,
            Consumer<GiraOneChannelValue> consumer) {
        return this.channelValues.filter(f -> f.getChannelViewUrn().equals(channel.getUrn())).subscribe(consumer);
    }
}
