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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration describes channel type for {@link GiraOneProjectItem}
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public enum GiraOneChannelType {

    Shutter("de.gira.schema.channels.BlindWithPos"),
    Dimmer("de.gira.schema.channels.KNX.Dimmer"),
    Float("de.gira.schema.channels.Float"),
    Switch("de.gira.schema.channels.Switch"),
    Function("de.gira.schema.channels.FunctionScene"),
    HeatingCooling("de.gira.schema.channels.KNX.HeatingCoolingSwitchable"),
    Unknown("Unknown");

    private final String name;

    GiraOneChannelType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneChannelType fromName(String value) throws IllegalArgumentException {
        return Arrays.stream(GiraOneChannelType.values()).filter(f -> value.equals(f.name)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
