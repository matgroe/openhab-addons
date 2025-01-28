/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

package org.openhab.binding.giraone.internal.discovery;

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = { UpnpDiscoveryParticipant.class })
public class GiraOneBridgeDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(GiraOneBridgeDiscoveryParticipant.class);
    private final static String DEVICE_NAMESPACE = "gira-de";
    private final static String DEVICE_TYPE = "Device";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    private boolean isValidRemoteDevice(RemoteDevice device) {
        return DEVICE_NAMESPACE.equals(device.getType().getNamespace())
                && DEVICE_TYPE.equals(device.getType().getType());
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (this.isValidRemoteDevice(device)) {
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            String model = device.getDetails().getModelDetails().getModelName();
            String serialNumber = device.getDetails().getModelDetails().getModelNumber();
            String udn = device.getIdentity().getUdn().getIdentifierString();
            String firmwareVersion = device.getDetails().getSerialNumber().split(",")[1];
            String host = device.getIdentity().getDescriptorURL().getHost();
            String label = String.format("%s (%s)", model, firmwareVersion);

            logger.info("Found device {} -- {}@{} :: FW='{}', SN#='{}'", udn, model, host, firmwareVersion,
                    serialNumber);

            return DiscoveryResultBuilder.create(Objects.requireNonNull(getThingUID(device)))
                    .withProperties(Map.of(HOST, host, Thing.PROPERTY_MODEL_ID, model, Thing.PROPERTY_VENDOR,
                            manufacturer, Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion,
                            Thing.PROPERTY_SERIAL_NUMBER, serialNumber.toLowerCase()))
                    .withLabel(label).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (this.isValidRemoteDevice(device)) {
            logger.info("getThingUID {}, {}", G1_SERVER_TYPE_UID,
                    device.getDetails().getModelDetails().getModelNumber().toLowerCase());
            return new ThingUID(G1_SERVER_TYPE_UID,
                    device.getDetails().getModelDetails().getModelNumber().toLowerCase());
        }
        return null;
    }
}
