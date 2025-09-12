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
import org.eclipse.jdt.annotation.NonNullByDefault;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_DATAPOINTS;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_NAME;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_URN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@link GiraOneBaseThingHandler} is responsible for handling the
 * communication between OpenHab and GiraOne
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public abstract class GiraOneBaseThingHandler extends BaseThingHandler {
    protected final CompositeDisposable disposables = new CompositeDisposable();

    private final Logger logger = LoggerFactory.getLogger(GiraOneBaseThingHandler.class);
    private static final String CONFIG_MISSING_CHANNEL_URN = "@text/giraone.thing.config.missing.channel-urn";
    private static final String PROJECT_MISSING_CHANNEL_URN = "@text/giraone.project.missing.channel-urn";

    public GiraOneBaseThingHandler(Thing thing) {
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
            disposables.add(getGiraOneBridge().subscribeOnConnectionState(this::onBridgeConnectionState));
        } catch (Exception exp) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_OFFLINE, exp.getMessage());
        }
    }

    @Override
    public void dispose() {
        disposables.clear();
        super.dispose();
    }

    @Override
    protected void updateState(String channelID, State state) {
        Optional<Channel> channel = thing.getChannels().stream()
                .filter(f -> channelID.equals(normalizeOpenhabChannelName(f.getUID().getId()))).findFirst();
        if (channel.isPresent()) {
            logger.debug("updateState ::  '{}' :: '{}'", channel.get().getUID(), state.toString());
            super.updateState(channel.get().getUID().getId(), state);
        } else {
            logger.info("updateState failed for '{}'. Cannot handle channel-id '{}'", this.thing, channelID);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        applyConfiguration();
    }

    /**
     * Handler function for receiving updates on the {@link GiraOneBridge} connection state.
     *
     * @param connectionState The {@link GiraOneBridgeState}.
     */
    void onBridgeConnectionState(GiraOneBridgeState connectionState) {
        switch (connectionState) {
            case Online -> this.bridgeMovedToOnline();
            case Offline -> this.bridgeMovedToOffline();
            case Error -> this.bridgeMovedToError();
        }
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeState#Online}
     */
    protected void bridgeMovedToOnline() {
        startObservingGiraOneChannel();
    }

    /**
     * Detects the channelUrn from the configuration
     * 
     * @return an Optional<String> which represents the channelUrn.
     */
    protected Optional<String> detectChannelUrn() {
        String channelUrn = getThing().getProperties().get(PROPERTY_CHANNEL_URN);
        return channelUrn != null ? Optional.of(channelUrn) : Optional.empty();
    }

    /**
     * Detects the channelUrn and performs a lookup for the concerning {@link GiraOneChannel}
     * within the {@link GiraOneProject}.
     *
     * @return an Optional<GiraOneChannel>
     */
    protected Optional<GiraOneChannel> lookupGiraOneProjectChannel() {
        GiraOneProject project = getGiraOneBridge().lookupGiraOneProject();
        Optional<String> channelUrn = detectChannelUrn();
        if (channelUrn.isPresent()) {
            logger.debug("lookupGiraOneProjectChannel :: try to lookup channel by urn {}", channelUrn.get());
            Optional<GiraOneChannel> channel = project.lookupChannelByUrn(channelUrn.get());
            if (channel.isPresent()) {
                return channel;
            } else {
                logger.warn("lookupGiraOneProjectChannel :: channel lookup by urn {} returned without result. ",
                        channelUrn.get());
            }
        } else {
            logger.debug("lookupGiraOneProjectChannel :: detectChannelUrn() returned empty channelUrn");
        }
        return Optional.empty();
    }

    protected GiraOneClientConfiguration getGiraOneClientConfiguration() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return bridge.getConfiguration().as(GiraOneClientConfiguration.class);
        }
        return getConfigAs(GiraOneClientConfiguration.class);
    }

    /**
     * Builds and Updates the Thing from GiraOneChannel
     */
    protected void buildAndUpdateThingDefinition() {
        this.lookupGiraOneProjectChannel().map(this::buildThing).ifPresent(this::updateThing);
    }

    /**
     * This function build the thing definition for OpenHab.
     *
     * @param channel The {@link GiraOneChannel}
     * @return The {@link Thing}
     */
    Thing buildThing(GiraOneChannel channel) {
        ThingBuilder thingBuilder = editThing();

        thingBuilder.withProperty(PROPERTY_FUNCTION_TYPE, channel.getFunctionType().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_TYPE, channel.getChannelType().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_TYPE_ID, channel.getChannelTypeId().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_DATAPOINTS, channel.getDataPoints().toString());

        if (getGiraOneClientConfiguration().overrideWithProjectSettings) {
            thingBuilder.withLabel(channel.getName());
            thingBuilder.withLocation(channel.getLocation());
        }
        thingBuilder.withChannels(determineSupportedChannels(channel));

        return thingBuilder.build();
    }

    private List<Channel> determineSupportedChannels(GiraOneChannel channel) {
        List<Channel> existingChannels = thing.getChannels();
        if (thing.getThingTypeUID().equals(GENERIC_TYPE_UID)) {
            List<String> supportedDataPoints = channel.getDataPoints().stream().map(GiraOneDataPoint::getName)
                    .map(CaseFormatter::lowerCaseHyphen).toList();

            List<String> filtered = existingChannels.stream().map((f -> f.getUID().getId()))
                    .map(this::normalizeOpenhabChannelName).filter(supportedDataPoints::contains).toList();

            return existingChannels.stream()
                    .filter(c -> filtered.contains(normalizeOpenhabChannelName(c.getUID().getId()))).toList();
        }
        return existingChannels;
    }

    private void startObservingGiraOneChannel() {
        Optional<GiraOneChannel> channel = lookupGiraOneProjectChannel();
        if (channel.isEmpty()) {
            Optional<String> channelUrn = detectChannelUrn();
            if (channelUrn.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, CONFIG_MISSING_CHANNEL_URN);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("%s [\"%s\"]", PROJECT_MISSING_CHANNEL_URN, channelUrn.get()));
            }
        } else {
            logger.trace("startObservingGiraOneChannel :: {}", channel.get());
            buildAndUpdateThingDefinition();
            updateStatus(ThingStatus.ONLINE);
            subscribeOnGiraOneDataPointValues(channel.get().getDataPoints(), this.disposables);
            getGiraOneBridge().lookupGiraOneChannelValues(channel.get());
        }
    }

    /**
     * Register callbacks to {@link GiraOneValue} for {@link GiraOneDataPoint} we're responsible for.
     *
     * @param datapoints The datapoints, we're responsible for
     * @param disposables The CompositeDisposable for all registered subscriptions.
     */
    protected void subscribeOnGiraOneDataPointValues(Collection<GiraOneDataPoint> datapoints,
            CompositeDisposable disposables) {
        datapoints.stream().map(GiraOneDataPoint::getUrn).distinct().map(dp -> dp.toString())
                .map(dp -> getGiraOneBridge().subscribeOnGiraOneDataPointValues(dp, this::onGiraOneValue))
                .forEach(disposables::add);
    }

    /**
     * Handler function for receiving {@link GiraOneValue} which contains
     * the value for an item channel.
     *
     * @param value The value to apply on a Openhab Thing
     */
    protected void onGiraOneValue(GiraOneValue value) {
        logger.info("empty implementation for onGiraOneValue :: {}", value);
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeState#Offline}
     */
    protected void bridgeMovedToOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    /**
     * Callback, if {@link GiraOneBridge} moved to {@link GiraOneBridgeState#Error}
     */
    protected void bridgeMovedToError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge is moved into error state");
    }

    private Optional<Object> getConfigurationValue(String key) {
        return getConfig().containsKey(key) ? Optional.of(getConfig().get(key)) : Optional.empty();
    }

    protected void applyConfiguration() {
        invalidateThingProperties();

        getConfigurationValue(PROPERTY_CHANNEL_URN)
                .ifPresent(val -> updateProperty(PROPERTY_CHANNEL_URN, val.toString()));

        getConfigurationValue(PROPERTY_CHANNEL_NAME).ifPresent(val -> getThing().setLabel(val.toString()));
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

    /**
     * Extracts the name from the openhab channel. If we're using channel groups,
     * the channel name occurs after the '#'
     *
     * @param name The openhab channel name.
     *
     * @return The channel name
     */
    protected String normalizeOpenhabChannelName(final String name) {
        String[] parts = name.split("#");
        return parts[parts.length - 1];
    }
}
