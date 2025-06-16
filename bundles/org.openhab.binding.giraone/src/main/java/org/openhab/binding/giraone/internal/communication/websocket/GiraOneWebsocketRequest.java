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
package org.openhab.binding.giraone.internal.communication.websocket;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Defines the command message to be sent out to Gira One Server.
 * It contains the {@link GiraOneCommand}, which defines the command
 * name and the property name within the command response json.
 * The unique commandId is built from command.name and some timestamp
 * information to map the received response to the requested server command.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebsocketRequest {
    private static final String PROPERTY_COMMAND_ID = "_gdsqueryId";
    private static final String PROPERTY_COMMAND_NAME = "command";

    @SerializedName(value = "request")
    private final JsonObject request;

    public GiraOneWebsocketRequest(GiraOneCommand command) {
        request = (JsonObject) GsonMapperFactory.createGson().toJsonTree(command);
        request.addProperty(PROPERTY_COMMAND_ID, GiraOneWebsocketSequence.next());
        request.addProperty(PROPERTY_COMMAND_NAME, command.getCommand());
    }

    public GiraOneCommand getCommand() {
        return Objects.requireNonNullElse(GsonMapperFactory.createGson().fromJson(request, GiraOneCommand.class),
                new GiraOneCommand());
    }

    public Integer getCommandId() {
        return request.getAsJsonPrimitive(PROPERTY_COMMAND_ID).getAsInt();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof GiraOneWebsocketRequest that)) {
            return false;
        }
        return Objects.equals(getCommandId(), that.getCommandId()) && Objects.equals(getCommand(), that.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(request);
    }
}
