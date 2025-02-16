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

import java.util.Collection;

import com.google.gson.annotations.SerializedName;

/**
 * GiraOneProjectChannel
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneProjectChannel {

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

    @SerializedName(value = "iconID")
    private int iconId;

    private Collection<GiraOneDataPoint> dataPoints;

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

    public int getIconId() {
        return iconId;
    }

    public Collection<GiraOneDataPoint> getDataPoints() {
        return dataPoints;
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', functionType=%s, channelType=%s, channelTypeId=%s, dataPoints=%s}",
                getClass().getSimpleName(), name, functionType, channelType, channelTypeId,
                dataPoints.stream().map(GiraOneDataPoint::getDataPoint).toList());
    }
}
