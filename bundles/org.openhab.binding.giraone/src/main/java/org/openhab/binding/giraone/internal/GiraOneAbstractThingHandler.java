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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private static final String CONFIG_MISSING_CHANNEL_URN = "@text/giraone.thing.config.missing.channel-urn";
    private static final String PROJECT_MISSING_CHANNEL_URN = "@text/giraone.project.missing.channel-urn";

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
            logger.debug("updateState ::  '{}' :: '{}'", channel.get().getUID(), state.toString());
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
            logger.debug("detectGiraOneChannel :: try to lookup channel by urn {}", channelUrn.get());
            Optional<GiraOneChannel> channel = project.lookupChannelByUrn(channelUrn.get());
            if (channel.isPresent()) {
                return channel;
            } else {
                logger.warn("detectGiraOneChannel :: channel lookup by urn {} returned without result. ",
                        channelUrn.get());
            }
        }
        return Optional.empty();
    }

    protected GiraOneClientConfiguration getGiraOneClientConfiguration() {
        if (getBridge() != null) {
            return (getBridge()).getConfiguration().as(GiraOneClientConfiguration.class);
        }
        return getConfigAs(GiraOneClientConfiguration.class);
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
