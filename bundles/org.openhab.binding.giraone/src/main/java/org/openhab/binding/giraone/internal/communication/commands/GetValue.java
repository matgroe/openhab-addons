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
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketRequest;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

/**
 * {@link GiraOneWebsocketRequest} for reading a datapoint value from Gira One Server.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "GetValue")
public class GetValue extends GiraOneCommand {
    public static GenericBuilder<GetValue> builder() {
        return GenericBuilder.of(GetValue::new);
    }

    private @Nullable Integer id;
    private @Nullable String urn;

    private String internal = "true";

    protected GetValue() {
    }

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public @Nullable String getUrn() {
        return urn;
    }

    public void setUrn(@Nullable String urn) {
        this.urn = urn;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }
}
