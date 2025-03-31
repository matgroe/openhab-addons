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
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * GiraOneChannel
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneChannel {

    @SerializedName(value = "channelID")
    private int channelId;
    private String channelUrn;

    @SerializedName(value = "channelViewID")
    private int channelViewId;
    private String channelViewUrn;
    private GiraOneFunctionType functionType;
    private GiraOneChannelType channelType;
    private GiraOneChannelTypeId channelTypeId;
    private String name;

    private final Collection<GiraOneDataPoint> dataPoints = new ArrayList<>();

    public int getChannelId() {
        return channelId;
    }

    public String getChannelUrn() {
        return channelUrn;
    }

    public int getChannelViewId() {
        return channelViewId;
    }

    public String getChannelViewUrn() {
        return channelViewUrn;
    }

    public GiraOneFunctionType getFunctionType() {
        return functionType;
    }

    public GiraOneChannelType getChannelType() {
        return channelType;
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

    @Override
    public String toString() {
        return String.format("%s{name='%s', functionType=%s, channelType=%s, channelTypeId=%s, dataPoints=%s}",
                getClass().getSimpleName(), name, functionType, channelType, channelTypeId,
                dataPoints.stream().map(GiraOneDataPoint::toString).toList());
    }
}
