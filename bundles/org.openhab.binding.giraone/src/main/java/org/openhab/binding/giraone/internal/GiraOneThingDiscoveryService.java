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

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.dto.GiraOneProject;
import org.openhab.binding.giraone.internal.dto.GiraOneProjectChannel;
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
    private static final int BACKGROUND_DISCOVERY_DELAY = 5;

    private final Logger logger = LoggerFactory.getLogger(GiraOneThingDiscoveryService.class);

    private ThingUID bridgeUID = new ThingUID(GiraOneBindingConstants.BRIDGE_TYPE_UID, "unknown");
    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob = null;
    private @Nullable GiraOneBridge giraOneBridge;
    private long timestampLastDiscovery = Instant.now().toEpochMilli();
    private Disposable disposableOnConnectionState = Disposable.empty();

    public GiraOneThingDiscoveryService() throws IllegalArgumentException {
        super(GiraOneBridgeHandler.class, TIMEOUT);
    }

    @Override
    public void initialize() {
        if (getThingHandler() != null) {
            bridgeUID = Objects.requireNonNull(getThingHandler()).getThing().getUID();
            giraOneBridge = ((GiraOneBridgeHandler) getThingHandler());
            disposableOnConnectionState = Objects.requireNonNull(giraOneBridge)
                    .subscribeOnConnectionState(this::onConnectionStateChanged);
        }

        super.initialize();
    }

    @Override
    public void dispose() {
        removeOlderResults(Instant.now().toEpochMilli(), bridgeUID);
        disposableOnConnectionState.dispose();
        disposableOnConnectionState = Disposable.empty();
        super.dispose();
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopBackgroundScanning();
    }

    @Override
    protected void startScan() {
        backgroundDiscoveryJob = scheduler.schedule(runnable, BACKGROUND_DISCOVERY_DELAY, TimeUnit.SECONDS);
    }

    private void discoverDevices() {
        try {
            GiraOneProject project = Objects.requireNonNull(giraOneBridge).lookupGiraOneProject();
            project.lookupChannels().stream().map(this::createDiscoverResultFromChannel).forEach(this::thingDiscovered);
        } catch (IllegalStateException exp) {
            logger.warn("Discovery of Devices failed :: {}", exp.getMessage());
        } finally {
            removeOlderResults(this.timestampLastDiscovery, bridgeUID);
            this.timestampLastDiscovery = Instant.now().toEpochMilli();
        }
    }

    private String formatThingTypeId(GiraOneProjectChannel channel) {
        switch (channel.getFunctionType()) {
            case Status -> {
                return CaseFormatter
                        .lowerCaseHyphen(channel.getFunctionType().toString() + channel.getChannelTypeId().toString());
            }
            default -> {
                return CaseFormatter
                        .lowerCaseHyphen(channel.getChannelType().toString() + channel.getChannelTypeId().toString());
            }
        }
    }

    ThingTypeUID detectThingTypeUID(GiraOneProjectChannel channel) {
        String thingTypeId = formatThingTypeId(channel);
        Optional<ThingTypeUID> opt = GiraOneBindingConstants.SUPPORTED_THING_TYPE_UID.stream()
                .filter(t -> t.getId().equals(thingTypeId)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return GiraOneBindingConstants.GENERIC_TYPE_UID;
        }
    }

    private DiscoveryResult createDiscoverResultFromChannel(GiraOneProjectChannel channel) {
        ThingTypeUID thingTypeUid = detectThingTypeUID(channel);
        logger.debug("{} maps to ThingTypeUID {}", channel, thingTypeUid);

        Map<String, Object> properties = new HashMap<>(20);
        properties.put("timestamp", new Date().toString());
        properties.put("channelId", channel.getChannelId());
        properties.put("channelUrn", channel.getChannelUrn());
        properties.put("channelViewId", channel.getChannelViewId());
        properties.put("channelViewUrn", channel.getChannelViewUrn());
        properties.put("functionType", channel.getFunctionType().getName());
        properties.put("channelType", channel.getChannelType().getName());
        properties.put("channelTypeId", channel.getChannelTypeId().getName());

        String label = channel.getName();
        String thingId = String.format("%d", channel.getChannelViewId());

        return DiscoveryResultBuilder.create(new ThingUID(thingTypeUid, bridgeUID, thingId)).withLabel(label)
                .withBridge(bridgeUID).withProperties(properties).withRepresentationProperty("channelViewUrn").build();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Running DeviceDiscovery ....");
            discoverDevices();
        }
    };

    protected synchronized void stopBackgroundScanning() {
        if (backgroundDiscoveryJob != null) {
            backgroundDiscoveryJob.cancel(true);
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
            discoverDevices();
        } else {
            stopBackgroundDiscovery();
        }
    }
}
