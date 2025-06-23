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
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.PROPERTY_FUNCTION_TYPE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.SUPPORTED_THING_TYPE_UID;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneConnectionState;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * The {@link GiraOneThingDiscoveryService}
 *
 * @author Matthias Groeger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GiraOneThingDiscoveryService.class)
@NonNullByDefault
public class GiraOneThingDiscoveryService extends AbstractThingHandlerDiscoveryService<GiraOneBridgeHandler> {
    private static final int TIMEOUT = 60;
    private static final int BACKGROUND_DISCOVERY_DELAY = 10;
    private static final int BACKGROUND_DISCOVERY_PERIOD = 1800;
    private static final int DISCOVERY_TTL_FACTOR = 2;

    private final Logger logger = LoggerFactory.getLogger(GiraOneThingDiscoveryService.class);

    private ThingUID bridgeUID = new ThingUID(GiraOneBindingConstants.BRIDGE_TYPE_UID, "unknown");
    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob = null;
    private @Nullable GiraOneBridge giraOneBridge;

    private Disposable disposableOnConnectionState = Disposable.empty();
    private boolean discoveryEnabled = false;

    public GiraOneThingDiscoveryService() throws IllegalArgumentException {
        super(GiraOneBridgeHandler.class, SUPPORTED_THING_TYPE_UID, TIMEOUT, false);
    }

    @Override
    public void initialize() {
        if (getThingHandler() != null) {
            discoveryEnabled = getThingHandler().getThing().getConfiguration()
                    .as(GiraOneClientConfiguration.class).discoverDevices;
            bridgeUID = Objects.requireNonNull(getThingHandler()).getThing().getUID();
            giraOneBridge = ((GiraOneBridgeHandler) getThingHandler());
            disposableOnConnectionState = Objects.requireNonNull(giraOneBridge)
                    .subscribeOnConnectionState(this::onConnectionStateChanged);
        }
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        this.stopBackgroundDiscovery();
        disposableOnConnectionState.dispose();
    }

    @Override
    protected void startScan() {
        this.discoverDevices();
    }

    private void discoverDevices() {
        if (discoveryEnabled) {
            try {
                GiraOneProject project = Objects.requireNonNull(giraOneBridge).lookupGiraOneProject();
                project.lookupChannels().stream().map(this::createDiscoverResultFromChannel)
                        .forEach(this::thingDiscovered);
            } catch (IllegalStateException exp) {
                logger.warn("Discovery of Devices failed :: {}", exp.getMessage());
            }
        }
    }

    String formatThingTypeId(GiraOneChannel channel) {
        if (channel.getFunctionType() == GiraOneFunctionType.Status) {
            return CaseFormatter
                    .lowerCaseHyphen(channel.getFunctionType().toString() + channel.getChannelTypeId().toString());
        }
        return CaseFormatter
                .lowerCaseHyphen(channel.getChannelType().toString() + channel.getChannelTypeId().toString());
    }

    ThingTypeUID detectThingTypeUID(GiraOneChannel channel) {
        String thingTypeId = formatThingTypeId(channel);
        Optional<ThingTypeUID> opt = GiraOneBindingConstants.SUPPORTED_THING_TYPE_UID.stream()
                .filter(t -> t.getId().equals(thingTypeId)).findFirst();

        return opt.isPresent() ? opt.get() : GiraOneBindingConstants.GENERIC_TYPE_UID;
    }

    private String generateIdentifier(GiraOneChannel channel) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(channel.getChannelViewUrn().getBytes());

            byte[] digest = md5.digest();
            String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
            return myHash.toLowerCase().substring(0, Math.min(myHash.length(), 10));
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot generate identifier.", e);
            return String.format("%d", Objects.hash(channel));
        }
    }

    private DiscoveryResult createDiscoverResultFromChannel(GiraOneChannel channel) {
        ThingTypeUID thingTypeUid = detectThingTypeUID(channel);
        logger.debug("{} maps to ThingTypeUID {}", channel, thingTypeUid);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_FUNCTION_TYPE, channel.getFunctionType().getName());
        properties.put(PROPERTY_CHANNEL_TYPE, channel.getChannelType().getName());
        properties.put(PROPERTY_CHANNELVIEW_URN, channel.getChannelViewUrn());
        properties.put(PROPERTY_CHANNEL_TYPE_ID, channel.getChannelTypeId().getName());

        String thingId = generateIdentifier(channel);
        return DiscoveryResultBuilder.create(new ThingUID(thingTypeUid, bridgeUID, thingId))
                .withLabel(channel.getName()).withBridge(bridgeUID).withProperties(properties)
                .withTTL(DISCOVERY_TTL_FACTOR * BACKGROUND_DISCOVERY_PERIOD)
                .withRepresentationProperty(PROPERTY_CHANNELVIEW_URN).build();
    }

    protected synchronized void startBackgroundScanning() {
        // just for getting sure
        stopBackgroundScanning();
        if (discoveryEnabled) {
            backgroundDiscoveryJob = this.scheduler.scheduleAtFixedRate(this::discoverDevices,
                    BACKGROUND_DISCOVERY_DELAY, BACKGROUND_DISCOVERY_PERIOD, TimeUnit.SECONDS);
        } else {
            logger.info("Service Discovery is Disabled by Configuration");
        }
    }

    protected synchronized void stopBackgroundScanning() {
        if (backgroundDiscoveryJob != null) {
            Objects.requireNonNull(backgroundDiscoveryJob).cancel(true);
        }
        backgroundDiscoveryJob = null;
        removeOlderResults(Instant.now().toEpochMilli());
    }

    @Override
    public void deactivate() {
        logger.info("deactivate");
        stopBackgroundScanning();
    }

    private void onConnectionStateChanged(GiraOneConnectionState connectionState) {
        logger.info("ConnectionStateChanged to {}", connectionState);
        if (connectionState == GiraOneConnectionState.Connected) {
            startBackgroundScanning();
        } else if (connectionState == GiraOneConnectionState.Disconnected
                || connectionState == GiraOneConnectionState.Error) {
            stopBackgroundDiscovery();
        }
    }
}
