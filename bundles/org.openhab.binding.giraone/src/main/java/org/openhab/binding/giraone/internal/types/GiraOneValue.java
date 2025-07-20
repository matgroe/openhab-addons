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
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

/**
 * The {@link GiraOneValue} represents a value for a single source of data
 * within the Gira One project. The GiraOneWebsocketClient emits
 * a {@link GiraOneValue} as result of sending GetValue command.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneValue {

    /**
     * The value as received from Gira One Server.
     */
    private final String value;

    /**
     * The datapoint urn this value belongs to.
     */
    private final GiraOneURN datapointUrn;

    public GiraOneValue(String datapointUrn, String value) {
        this.datapointUrn = GiraOneURN.of(datapointUrn);
        this.value = value;
    }

    public String getDatapointUrn() {
        return datapointUrn.toString();
    }

    public GiraOneDataPoint getGiraOneDataPoint() {
        return new GiraOneDataPoint(datapointUrn.toString());
    }

    public String getValue() {
        return value;
    }

    public boolean getValueAsBoolean() {
        return "1".equals(getValue());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GiraOneValue that = (GiraOneValue) o;
        return Objects.equals(value, that.value) && Objects.equals(datapointUrn, that.datapointUrn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, datapointUrn);
    }

    @Override
    public String toString() {
        return String.format("{urn=%s, value=%s}", datapointUrn, value);
    }
}
