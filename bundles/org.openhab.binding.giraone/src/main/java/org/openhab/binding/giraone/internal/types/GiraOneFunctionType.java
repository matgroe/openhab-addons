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
 * The enumeration of GiraOneFunctionTypes
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault()
public enum GiraOneFunctionType {
    Trigger("de.gira.schema.functions.Trigger"),
    PressAndHold("de.gira.schema.functions.PressAndHold"),
    Light("de.gira.schema.functions.KNX.Light"),
    Covering("de.gira.schema.functions.Covering"),
    HeatingCooling("de.gira.schema.functions.KNX.HeatingCooling"),
    Status("de.gira.schema.functions.NumericFloatStatus"),
    Scene("de.gira.schema.functions.FunctionScene"),
    Switch("de.gira.schema.functions.Switch"),
    Unknown("Unknown");

    private final String name;

    GiraOneFunctionType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneFunctionType fromName(String value) throws IllegalArgumentException {
        return Arrays.stream(GiraOneFunctionType.values()).filter(f -> value.equals(f.name)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
