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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketClient;

/**
 * The {@link GiraOneValueChange} represents value change for a single source of data. The
 * {@link GiraOneWebsocketClient} emits {@link GiraOneValueChange}
 * as a result after received a {@link GiraOneEvent}
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneValueChange extends GiraOneValue {

    /**
     * The previous value, the current value is available via super class
     */
    private final String previousValue;

    public GiraOneValueChange(int id, String value, String previous) {
        super(id, value);
        this.previousValue = previous;
    }

    /**
     * @return The previous value
     */
    public String getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, oldValue=%s, newValue=%s}", getId(), previousValue, getValue());
    }
}
