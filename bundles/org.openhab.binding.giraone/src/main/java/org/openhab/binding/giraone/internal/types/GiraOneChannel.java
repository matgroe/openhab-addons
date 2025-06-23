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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * GiraOneChannel
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneChannel {
    private String urn;
    private String name;
    private int channelViewId = 0;

    private GiraOneFunctionType functionType;
    private GiraOneChannelType channelType;

    private GiraOneChannelTypeId channelTypeId;
    private Collection<GiraOneDataPoint> dataPoints = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void setUrn(String urn) {
        this.urn = urn;
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

    public String getUrn() {
        return urn;
    }

    public String getChannelViewUrn() {
        return urn;
    }

    public GiraOneChannelTypeId getChannelTypeId() {
        return channelTypeId;
    }

    public String getName() {
        return name;
    }

    public boolean containsGiraOneDataPoint(int datapointId) {
        return this.dataPoints.stream().anyMatch(f -> f.getId() == datapointId);
    }

    public Optional<GiraOneDataPoint> getGiraOneDataPoint(int datapointId) {
        return this.dataPoints.stream().filter(f -> f.getId() == datapointId).findFirst();
    }

    public Collection<GiraOneDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(Collection<GiraOneDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void setChannelViewId(int channelViewId) {
        this.channelViewId = channelViewId;
    }

    public int getChannelViewId() {
        return channelViewId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GiraOneChannel that))
            return false;
        return Objects.equals(urn, that.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(urn);
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', functionType=%s, channelType=%s, channelTypeId=%s, dataPoints=%s}",
                getClass().getSimpleName(), name, functionType, channelType, channelTypeId,
                dataPoints.stream().map(GiraOneDataPoint::toString).toList());
    }
}
