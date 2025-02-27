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
 * The GiraOneValue represents a changed value for a single source of data
 * within the Gira One project. If offers the current ({@link GiraOneValue#getValue()})
 * and the previous ({@link GiraOneValueChange#getPreviousValue()})value as well.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneValueChange extends  GiraOneValue {

    /**
     * The previous value, the current value is available in super class
     */
    private final Object previousValue;

    public GiraOneValueChange(int id, Object value, Object previous) {
        super(id, value);
        this.previousValue = previous;
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return String.format("{id=%d, value=%s, previousValue=%s}", getId(), getValue(), previousValue);
    }
}
