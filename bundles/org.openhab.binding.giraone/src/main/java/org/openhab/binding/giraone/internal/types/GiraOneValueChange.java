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

/**
 * The {@link GiraOneValueChange} represents value change for a single source of data. The
 * GiraOneWebsocketClient emits {@link GiraOneValueChange} as a result after
 * received a GiraOneEvent.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneValueChange extends GiraOneValue {

    /**
     * The previous value, the current value is available via super class
     */
    private final String previousValue;

    public GiraOneValueChange(String urn, String value, String previous) {
        this(GiraOneURN.of(urn), value, previous);
    }

    public GiraOneValueChange(GiraOneURN urn, String value, String previous) {
        super(urn, value);
        this.previousValue = previous;
    }

    /**
     * @return returns true, if the previous value differs from current value
     */
    public boolean isChanged() {
        return !getValue().equals(getPreviousValue());
    }

    /**
     * Checks, if the value as represented by the given {@link GiraOneValueChange} is increasing or not
     *
     * @return returns true, if increasing, false otherwise.
     */
    public boolean isValueIncreasing() {
        return getValueAsFloat() > getPreviousValueAsFloat();
    }

    /**
     * @return The previous value
     */
    public String getPreviousValue() {
        return previousValue;
    }

    /**
     * @return The previous value as Number
     */
    public Number getPreviousValueAsNumber() {
        return Float.parseFloat(getPreviousValue());
    }

    /**
     * @return The previous value as float
     */
    public float getPreviousValueAsFloat() {
        return getPreviousValueAsNumber().floatValue();
    }

    /**
     * @return The previous value as int
     */
    public int getPreviousValueAsInt() {
        return getPreviousValueAsNumber().intValue();
    }

    @Override
    public String toString() {
        return String.format("{urn=%s, oldValue=%s, newValue=%s}", getDatapointUrn(), previousValue, getValue());
    }
}
