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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the current ProcessView with all datapoints and their values.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneProcessView {

    private final long creationTimestamp;
    private final List<GiraOneChannelDataPoint> datapoints = new ArrayList<>();

    public static GiraOneProcessView expired() {
        return new GiraOneProcessView(0L);
    }

    public GiraOneProcessView() {
        this(Instant.now().toEpochMilli());
    }

    private GiraOneProcessView(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public boolean isOlderThan(final long time, TimeUnit unit) {
        return (creationTimestamp + unit.toMillis(time)) - Instant.now().toEpochMilli() < 0;
    }

    public List<GiraOneChannelDataPoint> getDatapoints() {
        return datapoints;
    }
}
