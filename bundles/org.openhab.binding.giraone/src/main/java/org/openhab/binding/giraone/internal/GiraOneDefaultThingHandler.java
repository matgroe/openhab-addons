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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNELVIEW_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNELVIEW_URN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_NAME;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.binding.giraone.internal.util.ThingStateFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
            applyConfiguration();
            this.disposableOnConnectionState = getGiraOneBridge().subscribeOnConnectionState(this::onConnectionState);
        } catch (Exception exp) {
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

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        applyConfiguration();
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
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#TemporaryUnavailable}
     */
    protected void bridgeMovedToTemporaryUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge temporary offline.");
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Disconnected}
     */
    protected void bridgeMovedToDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeConnectionState#Error}
     */
    protected void bridgeMovedToError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge is moved into error state");
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

    private boolean configurationHasValue(Configuration configuration, String key) {
        return configuration.containsKey(key) && configuration.get(key) != null && !"".equals(configuration.get(key));
    }

    protected void applyConfiguration() {
        Configuration configuration = editConfiguration();
        updateThingProperties(Optional.empty());
        if (configurationHasValue(configuration, PROPERTY_CHANNELVIEW_URN)) {
            updateProperty(PROPERTY_CHANNELVIEW_URN, configuration.get(PROPERTY_CHANNELVIEW_URN).toString());
        }

        if (configurationHasValue(configuration, PROPERTY_CHANNELVIEW_ID)) {
            updateProperty(PROPERTY_CHANNELVIEW_ID, configuration.get(PROPERTY_CHANNELVIEW_ID).toString());
        }

        if (configurationHasValue(configuration, PROPERTY_CHANNEL_NAME)) {
            getThing().setLabel(configuration.get(PROPERTY_CHANNEL_NAME).toString());
        }
    }

    private Optional<String> detectChannelViewUrn() {
        String propChannelViewUrn = getThing().getProperties().get(PROPERTY_CHANNELVIEW_URN);
        if (propChannelViewUrn == null) {
            logger.warn("detectChannelViewUrn failed.");
            return Optional.empty();
        }
        return Optional.of(propChannelViewUrn);
    }

    /**
     *
     * @return an {@link Optional< GiraOneChannel >}
     */
    private Optional<GiraOneChannel> lookupGiraOneProjectChannel() {
        Optional<String> channelViewUrn = detectChannelViewUrn();
        if (channelViewUrn.isPresent()) {
            return getGiraOneBridge().lookupGiraOneProject().lookupChannelByChannelViewUrn(channelViewUrn.get());
        } else {
            if (getThing().getLabel() != null) {
                return getGiraOneBridge().lookupGiraOneProject()
                        .lookupChannelByName(Objects.requireNonNull(getThing().getLabel()));
            }
        }
        return Optional.empty();
    }

    private void updateThingProperties(Optional<GiraOneChannel> channel) {
        updateProperty(PROPERTY_FUNCTION_TYPE,
                channel.map(GiraOneChannel::getFunctionType).orElse(GiraOneFunctionType.Unknown).getName());
        updateProperty(PROPERTY_CHANNEL_TYPE,
                channel.map(GiraOneChannel::getChannelType).orElse(GiraOneChannelType.Unknown).getName());
        updateProperty(PROPERTY_CHANNEL_TYPE_ID,
                channel.map(GiraOneChannel::getChannelTypeId).orElse(GiraOneChannelTypeId.Unknown).getName());
        updateProperty(PROPERTY_CHANNELVIEW_ID,
                String.format("%d", channel.map(GiraOneChannel::getChannelViewId).orElse(0)));
    }

    protected int detectChannelViewId() {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        if (channel.isEmpty()) {
            String propChannelViewId = getThing().getProperties().get(PROPERTY_CHANNELVIEW_ID);
            if (propChannelViewId == null || "".equals(propChannelViewId)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Cannot detect GiraOneChannel by channelViewUrn, label or name");
            } else {
                return Integer.parseInt(propChannelViewId);
            }
        } else {
            updateThingProperties(channel);
        }
        return channel.map(GiraOneChannel::getChannelViewId).orElse(0);
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
    public final void handleCommand(ChannelUID channelUID, Command command) {
        Optional<GiraOneDataPoint> datapoint = findGiraOneDataPointWithinChannelView(channelUID.getId());
        if (datapoint.isPresent()) {
            logger.debug("handleCommand :: channelUID={}, command={}", channelUID, command);
            switch ((Object) command) {
                case RefreshType cmd -> handleRefreshTypeCommand(cmd);
                case DecimalType cmd -> handleDecimalTypeCommand(datapoint.get(), cmd);
                case OnOffType cmd -> handleOnOffTypeCommand(datapoint.get(), cmd);
                case UpDownType cmd -> handleUpDownTypeCommand(datapoint.get(), cmd);
                case StopMoveType cmd -> handleStopMoveTypeCommand(datapoint.get(), cmd);
                case StringType cmd -> handleStringTypeCommand(datapoint.get(), cmd);
                case QuantityType<?> cmd -> handleQuantityTypeCommand(datapoint.get(), cmd);
                default -> throw new IllegalStateException("Unsupported Command '" + command.getClass().getSimpleName()
                        + "' with value of +" + command.toString());
            }
        } else {
            logger.debug("not responsible for handling handleCommand {}, {}", channelUID, command);
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link RefreshType} command from openhab.
     * 
     * @param command The {@link RefreshType} command.
     */
    protected void handleRefreshTypeCommand(RefreshType command) {
        logger.trace("handleRefreshTypeCommand :: channelViewId={}, command={}", this.channelViewId, command);
        getGiraOneBridge().lookupGiraOneChannelValues(this.channelViewId);
    }

    /**
     * Override this method for special tasks on receiving a {@link DecimalType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link DecimalType} command.
     */
    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        logger.trace("handleDecimalTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.intValue());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link OnOffType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link OnOffType} command.
     */
    protected void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        logger.trace("handleOnOffTypeCommand :: datapoint={}, command={}", datapoint.getId(), command.name());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, (command == OnOffType.ON ? 1 : 0));
    }

    /**
     * Override this method for special tasks on receiving a {@link UpDownType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link UpDownType} command.
     */
    protected void handleUpDownTypeCommand(GiraOneDataPoint datapoint, UpDownType command) {
        logger.trace("handleUpDownType :: datapoint={}, command={}", datapoint.getId(), command.name());
        switch (command) {
            case DOWN -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, 100);
            case UP -> getGiraOneBridge().setGiraOneDataPointValue(datapoint, 0);
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link StopMoveType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link StopMoveType} command.
     */
    protected void handleStopMoveTypeCommand(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.warn("handleStopMoveType is not implemented :: datapoint={}, command={}", datapoint.getId(),
                command.name());
    }

    /**
     * Override this method for special tasks on receiving a {@link StringType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link StringType} command.
     */
    protected void handleStringTypeCommand(GiraOneDataPoint datapoint, StringType command) {
        logger.warn("handleStringType :: datapoint={}, command={}", datapoint.getId(), command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link QuantityType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link QuantityType} command.
     */
    protected void handleQuantityTypeCommand(GiraOneDataPoint datapoint, QuantityType<?> command) {
        logger.warn("handleQuantityType is not implemented :: datapoint={}, command={}", datapoint.getId(), command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.floatValue());
    }
}
