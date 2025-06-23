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
package org.openhab.binding.giraone.internal.communication.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link GiraOneWebserviceRequest} wraps a {@link GiraOneCommand} to get sent out via
 * webservice to the Gira One Server.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebserviceRequest {
    @SerializedName(value = "data")
    private final JsonObject data;
    private final transient GiraOneCommand command;

    protected GiraOneWebserviceRequest(GiraOneCommand command) {
        data = (JsonObject) GsonMapperFactory.createGson().toJsonTree(command);
        this.command = command;
    }

    public GiraOneCommand getCommand() {
        return this.command;
    }
}
