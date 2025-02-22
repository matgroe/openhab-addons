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

import com.google.gson.annotations.SerializedName;

/**
 * The GiraOneDataPoint represents a single datapoint like
 * - OnOff
 * - Step-Up-Down
 * - Up-Down
 * - Movement
 * - Position
 * - Slat-Position
 * - Float (e.g. humidity, temperature)
 * and is getting mapped to {@link org.openhab.core.thing.Channel}
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneDataPoint {

    @SerializedName(value = "dataPoint")
    private String dataPointName;
    private int id;
    private String urn;
    private String value;

    public String getName() {
        return dataPointName;
    }

    public void setName(String dataPointName) {
        this.dataPointName = dataPointName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, dataPoint=%s, value=%s, urn=%s}", id, dataPointName, value, urn);
    }
}
