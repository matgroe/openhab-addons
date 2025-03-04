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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNELVIEW_URN;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.ThingStateFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * The {@link GiraOneDefaultThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneDefaultThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GiraOneDefaultThingHandler.class);
    private int channelViewId;

    @Nullable
    private ScheduledFuture<?> lookupValuesScheduler = null;

    private Disposable disposableOnDataPointState = Disposable.empty();
    private Disposable disposableOnConnectionState = Disposable.empty();

    public GiraOneDefaultThingHandler(Thing thing) {
        super(thing);
    }

    protected GiraOneBridge getGiraOneBridge() {
        Objects.requireNonNull(getBridge(), "getBridge() must not evaluate to null");
        return (GiraOneBridge) Objects.requireNonNull(Objects.requireNonNull(getBridge()).getHandler());
    }

    @Override
    public void initialize() {
        logger.debug("initialize {}", getThing().getUID());
        try {
            this.disposableOnConnectionState = getGiraOneBridge().subscribeOnConnectionState(this::onConnectionState);
        } catch (NullPointerException exp) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_OFFLINE, exp.getMessage());
        }
    }

    protected void disposeSubscriptions() {
        try {
            this.disposableOnConnectionState.dispose();
            this.disposableOnDataPointState.dispose();
        } finally {
            this.disposableOnConnectionState = Disposable.empty();
            this.disposableOnDataPointState = Disposable.empty();
        }
    }

    @Override
    public void dispose() {
        disposeSubscriptions();
        super.dispose();
    }

    @Override
    protected void updateState(String channelID, State state) {
        logger.debug("updateState ::  channelId='{}', state='{}'", channelID, state.toString());
        super.updateState(channelID, state);
    }

    /**
     * Builds the openhab channelId from the given {@link GiraOneDataPoint}. The
     * generated value must match the channel.id property in the thing.xml in order
     * to match the wanted openhab channel.
     *
     * @param dataPoint The {@link GiraOneDataPoint}
     * @return referencing id for the thing channel
     */
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        return CaseFormatter.lowerCaseHyphen(dataPoint.getName());
    }

    /**
     * Builds the openhab channelId from the given {@link GiraOneChannelValue}. The
     * generated value must match the channel.id property in the thing.xml in order
     * to match the wanted openhab channel.
     *
     * @param channelValue {@link GiraOneChannelValue}
     * @return referencing id for the thing channel
     */
    protected String buildThingChannelId(GiraOneChannelValue channelValue) {
        return buildThingChannelId(channelValue.getGiraOneDataPoint());
    }

    /**
     * Handler function for receiving {@link GiraOneChannelValue} which contains
     * the value for an item channel.
     * 
     * @param channelValue The value to apply on a Openhab Thing
     */
    protected void onGiraOneChannelValue(GiraOneChannelValue channelValue) {
        logger.debug("onGiraOneChannelValue :: {}", channelValue);
        if (channelValue.getGiraOneValue() != null) {
            String thingChannelId = buildThingChannelId(channelValue);
            updateState(thingChannelId,
                    ThingStateFactory.from(thingChannelId, channelValue.getGiraOneValue().getValue().toString()));
        }
    }

    /**
     * Handler function for receiving updates on the {@link GiraOneBridge} connection state.
     *
     * @param connectionState The {@link GiraOneBridgeConnectionState}.
     */
    private void onConnectionState(GiraOneBridgeConnectionState connectionState) {
        switch (connectionState) {
            case Connecting -> this.bridgeMovedToConnecting();
            case Connected -> this.bridgeMovedToConnected();
            case Disconnected -> this.bridgeMovedToDisconnected();
            case Error -> this.bridgeMovedToError();
            case TemporaryUnavailable -> this.bridgeMovedToTemporaryUnavailable();
        }
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Connecting}
     */
    protected void bridgeMovedToConnecting() {
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Connected}
     */
    protected void bridgeMovedToConnected() {
        this.channelViewId = detectChannelViewId();
        if (this.channelViewId > 0) {
            this.disposableOnDataPointState = getGiraOneBridge().subscribeOnGiraOneChannelValue(this.channelViewId,
                    this::onGiraOneChannelValue);
            startSchedulerForValueLookup();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void startSchedulerForValueLookup() {
        lookupValuesScheduler = this.scheduler.scheduleAtFixedRate(
                () -> getGiraOneBridge().lookupGiraOneChannelValues(this.channelViewId), 1, 3600, TimeUnit.SECONDS);
    }

    private void cancelSchedulerForValueLookup() {
        if (lookupValuesScheduler != null) {
            lookupValuesScheduler.cancel(true);
        }
        lookupValuesScheduler = null;
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#TemporaryUnavailable}
     */
    protected void bridgeMovedToTemporaryUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge temporary offline.");
        cancelSchedulerForValueLookup();
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Disconnected}
     */
    protected void bridgeMovedToDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        cancelSchedulerForValueLookup();
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Error}
     */
    protected void bridgeMovedToError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge is moved into error state");
        cancelSchedulerForValueLookup();
    }

    /**
     * Checks, if the value as represented by the given {@link GiraOneValueChange} is increasing or not
     *
     * @param valueChange
     *
     * @return returns true, if increasing, false otherwise.
     */
    protected boolean isValueIncreasing(GiraOneValueChange valueChange) {
        return Float.parseFloat(valueChange.getValue()) > Float.parseFloat(valueChange.getPreviousValue());
    }

    /**
     *
     * @return an {@link Optional< GiraOneChannel >}
     */
    protected Optional<GiraOneChannel> lookupGiraOneProjectChannel() {
        String channelViewUrn = Objects.requireNonNull(getThing().getProperties().get(PROPERTY_CHANNELVIEW_URN));
        return getGiraOneBridge().lookupGiraOneProject().lookupChannelByChannelViewUrn(channelViewUrn);
    }

    protected int detectChannelViewId() {
        int channelViewId = lookupGiraOneProjectChannel().map(GiraOneChannel::getChannelViewId).orElse(0);
        this.updateProperty("channelViewId", Integer.valueOf(channelViewId).toString());
        return channelViewId;
    }

    /**
     *
     * @param ohChannel the channel id in OpenHab context
     * @return
     */
    protected Optional<GiraOneDataPoint> findGiraOneDataPointWithinChannelView(final String ohChannel) {
        Optional<GiraOneChannel> channel = getGiraOneBridge().lookupGiraOneProject()
                .lookupChannelByChannelViewId(this.channelViewId);
        return channel.flatMap(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .filter(f -> CaseFormatter.lowerCaseHyphen(f.getName()).equals(ohChannel)).findFirst());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Optional<GiraOneDataPoint> datapoint = findGiraOneDataPointWithinChannelView(channelUID.getId());
        if (datapoint.isPresent()) {
            logger.debug("handleCommand :: channelUID={}, command={}", channelUID, command);
            switch ((Object) command) {
                case RefreshType cmd -> handleRefreshTypeCommand(cmd);
                case DecimalType cmd -> handleDecimalTypeCommand(datapoint.get(), cmd);
                case OnOffType cmd -> handleOnOffTypeCommand(datapoint.get(), cmd);
                case UpDownType cmd -> handleUpDownType(datapoint.get(), cmd);
                case StopMoveType cmd -> handleStopMoveType(datapoint.get(), cmd);
                case StringType cmd -> handleStringType(datapoint.get(), cmd);
                default -> throw new IllegalStateException("Unsupported value: " + (Object) command);
            }
        } else {
            logger.debug("not responsible for handling handleCommand {}, {}", channelUID, command);
        }
    }

    protected void handleRefreshTypeCommand(RefreshType command) {
        logger.trace("handleRefreshTypeCommand :: channelViewId={}, command={}", this.channelViewId, command);
        getGiraOneBridge().lookupGiraOneChannelValues(this.channelViewId);
    }

    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        logger.trace("handleDecimalTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.intValue());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    protected void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        logger.trace("handleOnOffTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.name());
        String value = command == OnOffType.ON ? "1" : "0";
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, value);
    }

    protected void handleUpDownType(GiraOneDataPoint datapoint, UpDownType command) {
        logger.trace("handleUpDownType :: datapoint={}, command={}", datapoint.getId(), command.name());
        switch (command) {
            case DOWN -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, Integer.toString(100));
            case UP -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, Integer.toString(0));
        }
    }

    protected void handleStopMoveType(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.warn("handleStopMoveType is not implemented :: datapoint={}, command={}", datapoint.getId(),
                command.name());
    }

    protected void handleStringType(GiraOneDataPoint datapoint, StringType command) {
        logger.warn("handleStringType is not implemented :: datapoint={}, command={}", datapoint.getId(),
                command.toString());
    }
}
