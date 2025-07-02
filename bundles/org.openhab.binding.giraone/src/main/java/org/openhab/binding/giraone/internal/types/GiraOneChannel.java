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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * GiraOneChannel
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneChannel {
    private String urn;
    private String name;
    private String location;

    private GiraOneFunctionType functionType;
    private GiraOneChannelType channelType;

    private GiraOneChannelTypeId channelTypeId;
    private Set<GiraOneDataPoint> dataPoints = Collections.synchronizedSet(new HashSet<>());

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getUrn() {
        return urn;
    }

    public void setFunctionType(GiraOneFunctionType functionType) {
        this.functionType = functionType;
    }

    public GiraOneFunctionType getFunctionType() {
        return functionType;
    }

    public void setChannelType(GiraOneChannelType channelType) {
        this.channelType = channelType;
    }

    public GiraOneChannelType getChannelType() {
        return channelType;
    }

    public void setChannelTypeId(GiraOneChannelTypeId channelTypeId) {
        this.channelTypeId = channelTypeId;
    }

    public GiraOneChannelTypeId getChannelTypeId() {
        return channelTypeId;
    }

    public boolean containsGiraOneDataPoint(String datapointUrn) {
        return this.dataPoints.stream().anyMatch(f -> datapointUrn.equals(f.getUrn()));
    }

    public Collection<GiraOneDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void addDataPoint(GiraOneDataPoint dataPoints) {
        this.dataPoints.add(dataPoints);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof GiraOneChannel that) {
            return Objects.equals(urn, that.urn);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(urn);
    }

    @Override
    public String toString() {
        return String.format(
                "%s{urn='%s', name='%s', functionType=%s, channelType=%s, channelTypeId=%s, dataPoints=%s}",
                getClass().getSimpleName(), urn, name, functionType, channelType, channelTypeId,
                dataPoints.stream().map(GiraOneDataPoint::toString).toList());
    }
}
