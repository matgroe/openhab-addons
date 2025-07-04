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

package org.openhab.binding.giraone.internal.types;

import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.HashMap;

/**
 * The {@link GiraOneDeviceConfiguration} class describes the gira one server
 * runtime configuration.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneDeviceConfiguration {
    public static final String CURRENT_APPLICATION_VERSION = "CurrentApplicationVersion";
    public static final String CURRENT_FIRMWARE_VERSION = "CurrentFirmwareVersion";
    public static final String CURRENT_SYSTEM = "CurrentSystem";
    public static final String DEVICE_NAME = "DeviceName";
    public static final String DEVICE_ID = "DeviceId";
    public static final String IP_ADDRESS = "IpAddress";

    @SerializedName("ipc")
    private final HashMap<String, String> configuration = new HashMap<>();

    public String get(String name) {
        return configuration.get(name);
    }
}
