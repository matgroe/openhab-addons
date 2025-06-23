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
package org.openhab.binding.giraone.internal.communication.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

/**
 * {@link GiraOneCommand} for setting a new value on a datapoint.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "SetValue")
public class SetValue extends GiraOneCommand {
    public static GenericBuilder<SetValue> builder() {
        return GenericBuilder.of(SetValue::new);
    }

    private @Nullable Integer id;
    private @Nullable Object value;

    protected SetValue() {
    }

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }
}
