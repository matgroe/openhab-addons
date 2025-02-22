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

/**
 * The {@link GiraOneChannelDataPoint} represents a single {@link GiraOneDataPoint}
 * within a {@link GiraOneProjectChannel}.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneChannelDataPoint extends GiraOneDataPoint {

    public GiraOneChannelDataPoint() {
        super();
    }

    public GiraOneChannelDataPoint(GiraOneDataPoint dataPoint) {
        this();
        this.setId(dataPoint.getId());
        this.setName(dataPoint.getName());
        this.setUrn(dataPoint.getUrn());
        this.setValue(dataPoint.getValue());
    }

    private int channelViewId;

    private String channelViewUrn;

    public int getChannelViewId() {
        return channelViewId;
    }

    public void setChannelViewId(int channelViewId) {
        this.channelViewId = channelViewId;
    }

    public String getChannelViewUrn() {
        return channelViewUrn;
    }

    public void setChannelViewUrn(String channelViewUrn) {
        this.channelViewUrn = channelViewUrn;
    }

    @Override
    public String toString() {
        return String.format("{channelViewId=%d, channelViewUrn=%s datapointId=%d, dataPoint=%s, urn=%s, value=%s}",
                getChannelViewId(), getChannelViewUrn(), super.getId(), super.getName(), super.getUrn(),
                super.getValue());
    }
}
