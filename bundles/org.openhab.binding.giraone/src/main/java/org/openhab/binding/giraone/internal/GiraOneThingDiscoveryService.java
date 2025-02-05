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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GiraOneThingDiscoveryService}
 *
 * @author Matthias Groeger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GiraOneThingDiscoveryService.class)
@NonNullByDefault
public class GiraOneThingDiscoveryService extends AbstractThingHandlerDiscoveryService<GiraOneBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(GiraOneThingDiscoveryService.class);

    private ThingUID bridgeUID = new ThingUID(GiraOneBindingConstants.BRIDGE_TYPE_UID, "unknown");

    private static final int TIMEOUT = 60;
    private static final int BACKGROUND_DISCOVERY_DELAY = 5;

    private @Nullable ScheduledFuture<?> backgroundDiscoveryJob = null;

    public GiraOneThingDiscoveryService() throws IllegalArgumentException {
        super(GiraOneBridgeHandler.class, TIMEOUT);
    }

    @Override
    public void initialize() {
        if (getThingHandler() != null) {
            bridgeUID = getThingHandler().getThing().getUID();
        }
        super.initialize();
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

    void discoverDevices() {
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            logger.info("Running DeviceDiscovery ....");
            ThingUID uid = new ThingUID(GiraOneBindingConstants.DEVICE_TYPE_UID, bridgeUID, "deviceId");
            Map<String, Object> properties = new HashMap<>(1);
            properties.put("deviceId", "device.deviceId");
            properties.put("interface", "device.interfaceType");

            String deviceLabel = "device.deviceLabel";

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(deviceLabel)
                    .withRepresentationProperty("deviceId").withBridge(bridgeUID).withProperties(properties).build();

            thingDiscovered(discoveryResult);
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
}
