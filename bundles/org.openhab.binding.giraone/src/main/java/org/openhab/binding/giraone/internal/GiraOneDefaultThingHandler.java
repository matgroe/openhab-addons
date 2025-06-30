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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_DATAPOINTS;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_NAME;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_URN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;

/**
 * The {@link GiraOneDefaultThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneDefaultThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GiraOneDefaultThingHandler.class);

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
            this.lookupGiraOneProjectChannel().map(this::updateThing);

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
        logger.debug("updateState ::  '{}' :: '{}={}'", this.thing, channelID, state.toString());
        Optional<Channel> channel = thing.getChannels().stream()
                .filter(f -> channelID.equals(normalizeOpenhabChannelName(f.getUID().getId()))).findFirst();
        if (channel.isPresent()) {
            logger.debug("updateState ::  '{}' :: '{}'", channel.get(), state.toString());
            super.updateState(channel.get().getUID().getId(), state);
        }
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
     * @param connectionState The {@link GiraOneConnectionState}.
     */
    private void onConnectionState(GiraOneConnectionState connectionState) {
        logger.trace("onConnectionState :: {}", connectionState);
        switch (connectionState) {
            case Connecting -> this.bridgeMovedToConnecting();
            case Connected -> this.bridgeMovedToConnected();
            case Disconnected -> this.bridgeMovedToDisconnected();
            case Error -> this.bridgeMovedToError();
            case TemporaryUnavailable -> this.bridgeMovedToTemporaryUnavailable();
        }
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneConnectionState#Connecting}
     */
    protected void bridgeMovedToConnecting() {
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneConnectionState#Connected}
     */
    protected void bridgeMovedToConnected() {
        startObservingGiraOneChannel();
    }

    private void startObservingGiraOneChannel() {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();

        if (channel.isEmpty()) {
            logger.info(
                    "startObservingGiraOneChannel failed. Received empty result from lookupGiraOneProjectChannel().");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot detect GiraOneChannel by channelUrn, label or name");
        } else {
            logger.trace("startObservingGiraOneChannel :: {}", channel.get());

            this.disposableOnDataPointState = getGiraOneBridge().subscribeOnGiraOneChannelValue(channel.get(),
                    this::onGiraOneChannelValue);
            getGiraOneBridge().lookupGiraOneChannelValues(channel.get());
        }
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneConnectionState#TemporaryUnavailable}
     */
    protected void bridgeMovedToTemporaryUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge temporary offline.");
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneConnectionState#Disconnected}
     */
    protected void bridgeMovedToDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneConnectionState#Error}
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
        invalidateThingProperties();
        if (configurationHasValue(configuration, PROPERTY_CHANNEL_URN)) {
            updateProperty(PROPERTY_CHANNEL_URN, configuration.get(PROPERTY_CHANNEL_URN).toString());
        }

        if (configurationHasValue(configuration, PROPERTY_CHANNEL_NAME)) {
            getThing().setLabel(configuration.get(PROPERTY_CHANNEL_NAME).toString());
        }
    }

    private Optional<String> detectChannelUrn() {
        String propChannelViewUrn = getThing().getProperties().get(PROPERTY_CHANNEL_URN);
        if (propChannelViewUrn == null) {
            logger.warn("detectChannelViewUrn failed.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot detect thing property " + PROPERTY_CHANNEL_URN);
            return Optional.empty();
        }
        return Optional.of(propChannelViewUrn);
    }

    /**
     *
     * @return an {@link Optional< GiraOneChannel >}
     */
    private Optional<GiraOneChannel> lookupGiraOneProjectChannel() {
        Optional<String> channelUrn = detectChannelUrn();
        Optional<GiraOneChannel> theChannel = Optional.empty();

        if (channelUrn.isPresent()) {
            logger.debug("startObservingGiraOneChannel :: try to lookup channel by urn {}", channelUrn.get());
            theChannel = getGiraOneBridge().lookupGiraOneProject().lookupChannelByUrn(channelUrn.get());
        }

        if (theChannel.isEmpty()) {
            if (getThing().getLabel() != null) {
                logger.debug("startObservingGiraOneChannel :: try to lookup channel by label {}",
                        getThing().getLabel());
                theChannel = getGiraOneBridge().lookupGiraOneProject()
                        .lookupChannelByName(Objects.requireNonNull(getThing().getLabel()));
            }
        }

        if (theChannel.isEmpty()) {
            logger.debug("startObservingGiraOneChannel :: channel lookup by urn or label.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Channel Lookup Failed.");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
        return theChannel;
    }

    private void invalidateThingProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_CHANNEL_URN,
                Objects.requireNonNullElse(this.thing.getProperties().get(PROPERTY_CHANNEL_URN), "n.a"));
        properties.put(PROPERTY_FUNCTION_TYPE, GiraOneFunctionType.Unknown.getName());
        properties.put(PROPERTY_CHANNEL_TYPE_ID, GiraOneChannelTypeId.Unknown.getName());
        properties.put(PROPERTY_CHANNEL_TYPE, GiraOneChannelType.Unknown.getName());
        updateProperties(properties);
    }

    GiraOneChannel updateThing(GiraOneChannel channel) {
        updateProperty(PROPERTY_FUNCTION_TYPE, channel.getFunctionType().getName());
        updateProperty(PROPERTY_CHANNEL_TYPE, channel.getChannelType().getName());
        updateProperty(PROPERTY_CHANNEL_TYPE_ID, channel.getChannelTypeId().getName());
        updateProperty(PROPERTY_CHANNEL_DATAPOINTS, channel.getDataPoints().toString());

        ThingBuilder thingBuilder = editThing();
        List<Channel> existingChannels = thing.getChannels();

        thingBuilder.withChannels(existingChannels);
        thingBuilder.withLabel(channel.getName());
        thingBuilder.withLocation(channel.getLocation());

        if (thing.getThingTypeUID().equals(GENERIC_TYPE_UID)) {
            List<String> supportedDataPoints = channel.getDataPoints().stream().map(GiraOneDataPoint::getName)
                    .map(CaseFormatter::lowerCaseHyphen).toList();

            List<String> filtered = existingChannels.stream().map((f -> f.getUID().getId()))
                    .map(this::normalizeOpenhabChannelName).filter(supportedDataPoints::contains).toList();

            List<Channel> supportedChannels = existingChannels.stream()
                    .filter(c -> filtered.contains(normalizeOpenhabChannelName(c.getUID().getId()))).toList();

            thingBuilder.withChannels(supportedChannels);
        }

        updateThing(thingBuilder.build());

        return channel;
    }

    /**
     * Extracts the name from the openhab channel. If we're using channel groups,
     * the channel name occurs after the '#'
     *
     * @param name The openhab channel name.
     *
     * @return The channel name
     */
    private String normalizeOpenhabChannelName(final String name) {
        String[] parts = name.split("#");
        return parts[parts.length - 1];
    }

    /**
     *
     * @param ohChannel the channel id in OpenHab context
     * @return
     */
    protected Optional<GiraOneDataPoint> findGiraOneDataPointWithinChannelView(final String ohChannel) {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        String channelId = normalizeOpenhabChannelName(ohChannel);
        return channel.flatMap(giraOneProjectChannel -> giraOneProjectChannel.getDataPoints().stream()
                .filter(f -> CaseFormatter.lowerCaseHyphen(f.getName()).equals(channelId)).findFirst());
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
                default -> throw new IllegalStateException(
                        "Unsupported Command '" + command.getClass().getSimpleName() + "' with value of +" + command);
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
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        if (channel.isPresent()) {
            logger.trace("handleRefreshTypeCommand :: channel={}, command={}", channel.get(), command);
            getGiraOneBridge().lookupGiraOneChannelValues(channel.get());
        }
    }

    /**
     * Override this method for special tasks on receiving a {@link DecimalType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link DecimalType} command.
     */
    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        logger.trace("handleDecimalTypeCommand :: datapoint={}, command={}", datapoint, command.intValue());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link OnOffType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link OnOffType} command.
     */
    protected void handleOnOffTypeCommand(GiraOneDataPoint datapoint, OnOffType command) {
        logger.trace("handleOnOffTypeCommand :: datapoint={}, command={}", datapoint, command.name());
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, (command == OnOffType.ON ? 1 : 0));
    }

    /**
     * Override this method for special tasks on receiving a {@link UpDownType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link UpDownType} command.
     */
    protected void handleUpDownTypeCommand(GiraOneDataPoint datapoint, UpDownType command) {
        logger.trace("handleUpDownType :: datapoint={}, command={}", datapoint, command.name());
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
        logger.warn("handleStopMoveType is not implemented :: datapoint={}, command={}", datapoint, command.name());
    }

    /**
     * Override this method for special tasks on receiving a {@link StringType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link StringType} command.
     */
    protected void handleStringTypeCommand(GiraOneDataPoint datapoint, StringType command) {
        logger.warn("handleStringType :: datapoint={}, command={}", datapoint, command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.toString());
    }

    /**
     * Override this method for special tasks on receiving a {@link QuantityType} command from openhab.
     *
     * @param datapoint The {@link GiraOneDataPoint}, the command should be applied on.
     * @param command The {@link QuantityType} command.
     */
    protected void handleQuantityTypeCommand(GiraOneDataPoint datapoint, QuantityType<?> command) {
        logger.warn("handleQuantityType is not implemented :: datapoint={}, command={}", datapoint, command);
        getGiraOneBridge().setGiraOneDataPointValue(datapoint, command.floatValue());
    }
}
