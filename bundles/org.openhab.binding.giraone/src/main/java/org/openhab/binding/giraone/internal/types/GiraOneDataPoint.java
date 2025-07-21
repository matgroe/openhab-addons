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

import java.util.Objects;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The GiraOneDataPoint defines a source of data which may have a
 * value.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneDataPoint {
    private GiraOneURN urn;

    public GiraOneDataPoint(final String urn) {
        this.urn = GiraOneURN.of(urn);
    }

    public String getName() {
        return urn.getResourceName();
    }

    public String getDeviceUrn() {
        return urn.getParent().toString();
    }

    public String getUrn() {
        return urn.toString();
    }

    public void setUrn(String urn) {
        this.urn = GiraOneURN.of(urn);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GiraOneDataPoint dataPoint = (GiraOneDataPoint) o;
        return Objects.equals(getUrn(), dataPoint.getUrn());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(urn);
    }

    @Override
    public String toString() {
        return getUrn();
    }
}
