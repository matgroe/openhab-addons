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
package org.openhab.binding.giraone.internal.dto;

import java.util.Arrays;

/**
 * Enumeration describes channel type id
 * for {@link GiraOneProjectItem}
 *
 * @author Matthias Gröger - Initial contribution
 */
public enum GiraOneChannelTypeId {
    Float("NumericFloatStatus.Temperatur"),
    FunctionScene("FunctionScene.Scene"),
    HeatingCoolingSwitchable("KNX.HeatingCooling.HeatingUnderfloorHeatingWaterBased"),
    Humidity("NumericFloatStatus.Humidity"),
    Light("KNX.Light.Light"),
    SwitchLamp("Switch.Lamp"),
    SwitchPowerOutlet("Switch.PowerOutlet"),
    CoveringAwning("Covering.Awning"),
    CoveringRoofWindow("Covering.RoofWindow"),
    CoveringVenetianBlind("Covering.VenetianBlind"),

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
