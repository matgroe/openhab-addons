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
 * GiraOneDataPointState
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneDataPointState extends GiraOneDataPoint {

    private int channelViewId;
    private String channelViewUrn;

    private Object state;

    private String valueState;

    public String getChannelViewUrn() {
        return channelViewUrn;
    }

    public void setChannelViewUrn(String channelViewUrn) {
        this.channelViewUrn = channelViewUrn;
    }

    public int getChannelViewId() {
        return channelViewId;
    }

    public void setChannelViewId(int channelViewId) {
        this.channelViewId = channelViewId;
    }

    public Object getState() {
        return state;
    }

    public void setState(Object state) {
        this.state = state;
    }

    public String getValueState() {
        return valueState;
    }

    public void setValueState(String valueState) {
        this.valueState = valueState;
    }

    @Override
    public String toString() {
        return String.format("{channelViewId=%d, datapointId=%d, dataPoint=%s, urn=%s, state=%s}", channelViewId,
                super.getId(), super.getDataPoint(), super.getUrn(), state);
    }
}
