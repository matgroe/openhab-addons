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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The GiraOneValue represents a value for a single source of data
 * within the Gira One project.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneValue {

    /**
     * The numeric id to reference the data point this value
     * belongs to.
     */
    private final int id;
    private final Object value;

    public GiraOneValue(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, value=%s}", id, value);
    }
}
