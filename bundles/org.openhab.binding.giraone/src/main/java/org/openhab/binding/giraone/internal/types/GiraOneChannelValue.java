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

/**
 * The {@link GiraOneChannelValue} represents a single {@link GiraOneDataPoint}
 * within a {@link GiraOneChannel}.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneChannelValue {

    private String channelViewUrn;

    private GiraOneDataPoint giraOneDataPoint = new GiraOneDataPoint();
    private GiraOneValue giraOneValue;

    public GiraOneDataPoint getGiraOneDataPoint() {
        return giraOneDataPoint;
    }

    public void setGiraOneDataPoint(GiraOneDataPoint giraOneDataPoint) {
        this.giraOneDataPoint = giraOneDataPoint;
    }

    public String getChannelViewUrn() {
        return channelViewUrn;
    }

    public void setChannelViewUrn(String channelViewUrn) {
        this.channelViewUrn = channelViewUrn;
    }

    public GiraOneValue getGiraOneValue() {
        return giraOneValue;
    }

    public void setGiraOneValue(GiraOneValue giraOneValue) {
        this.giraOneValue = giraOneValue;
    }

    @Override
    public String toString() {
        return String.format("{channelViewUrn=%s, datapointId=%d, dataPoint=%s, urn=%s, value=%s}",
                getChannelViewUrn(), giraOneDataPoint.getId(), giraOneDataPoint.getName(),
                giraOneDataPoint.getUrn(), getGiraOneValue());
    }

    public int getChannelViewId() {
        return 0;
    }
}
