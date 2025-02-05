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
 * GiraOneDataPoint maps to {@link org.openhab.core.thing.Channel}
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneDataPoint {

    @SerializedName(value = "dataPoint")
    private String dataPoint;
    private int id;
    private String urn;

    public String getDataPoint() {
        return dataPoint;
    }

    public int getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, dataPoint=%s, urn=%s}", id, dataPoint, urn);
    }
}
