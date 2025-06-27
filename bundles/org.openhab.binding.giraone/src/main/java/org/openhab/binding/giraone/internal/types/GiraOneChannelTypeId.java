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

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Arrays;

/**
 * Enumeration describes channel type id
 * for {@link GiraOneChannelTypeId}
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault()
public enum GiraOneChannelTypeId {
    Temperature("NumericFloatStatus.Temperatur"),
    Humidity("NumericFloatStatus.Humidity"),
    Underfloor("KNX.HeatingCooling.HeatingUnderfloorHeatingWaterBased"),
    Light("KNX.Light.Light"),
    Dimmer("KNX.Light.Dimmer"),
    Lamp("Switch.Lamp"),
    Pump("Switch.Pump"),
    PowerOutlet("Switch.PowerOutlet"),
    Awning("Covering.Awning"),
    RoofWindow("Covering.RoofWindow"),
    VenetianBlind("Covering.VenetianBlind"),
    Scene("FunctionScene.Scene"),
    Unknown("Unknown");

    private final String name;

    GiraOneChannelTypeId(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneChannelTypeId fromName(String value) throws IllegalArgumentException {
        return Arrays.stream(GiraOneChannelTypeId.values()).filter(f -> value.equals(f.name)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
