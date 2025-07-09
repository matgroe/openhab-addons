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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.GENERIC_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_DATAPOINTS;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_NAME;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_URN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * The {@link GiraOneAbstractThingHandler} is responsible for handling commands, which are
 * sent to one of the shutter channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public abstract class GiraOneAbstractThingHandler extends BaseThingHandler {
    protected final CompositeDisposable disposables = new CompositeDisposable();
    private final Logger logger = LoggerFactory.getLogger(GiraOneAbstractThingHandler.class);

    public GiraOneAbstractThingHandler(Thing thing) {
        super(thing);
    }

    protected GiraOneBridge getGiraOneBridge() {
        Objects.requireNonNull(getBridge(), "getBridge() must not evaluate to null");
        return (GiraOneBridge) Objects.requireNonNull(Objects.requireNonNull(getBridge()).getHandler());
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
     * Handler function for receiving updates on the {@link GiraOneBridge} connection state.
     *
     * @param connectionState The {@link GiraOneConnectionState}.
     */
    protected void onConnectionState(GiraOneConnectionState connectionState) {
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
    protected Optional<GiraOneChannel> lookupGiraOneProjectChannel() {
        Optional<GiraOneChannel> theChannel = Optional.empty();
        GiraOneProject project = getGiraOneBridge().lookupGiraOneProject();

        Optional<String> channelUrn = detectChannelUrn();
        if (channelUrn.isPresent()) {
            logger.debug("startObservingGiraOneChannel :: try to lookup channel by urn {}", channelUrn.get());
            theChannel = project.lookupChannelByUrn(channelUrn.get());
        }

        if (theChannel.isEmpty()) {
            String label = getThing().getLabel();
            if (label != null) {
                logger.debug("startObservingGiraOneChannel :: try to lookup channel by label {}", label);
                theChannel = project.lookupChannelByName(label);
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

    /**
     * This function build the thing definition for OpenHab.
     *
     * @param channel The
     *
     * @return The channel
     */
    Thing buildThing(GiraOneChannel channel) {
        ThingBuilder thingBuilder = editThing();
        List<Channel> existingChannels = thing.getChannels();
        thingBuilder.withProperty(PROPERTY_FUNCTION_TYPE, channel.getFunctionType().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_TYPE, channel.getChannelType().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_TYPE_ID, channel.getChannelTypeId().getName());
        thingBuilder.withProperty(PROPERTY_CHANNEL_DATAPOINTS, channel.getDataPoints().toString());

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
        return thingBuilder.build();
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
    protected abstract void subscribeOnGiraOneDataPointValues(Collection<GiraOneDataPoint> datapoints,
            CompositeDisposable disposables);

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
