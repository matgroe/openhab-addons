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
package org.openhab.binding.giraone.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;

/**
 * This class represents a command response as received from the Gira One Sever
 * as to an received {@link ServerCommand}.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneCommandResponse {
    final static String PROPERTY_REQUEST = "request";
    final JsonObject responseBody;

    public GiraOneCommandResponse(final JsonObject responseBody) {
        this.responseBody = responseBody;
    }

    public ServerCommand getRequestServerCommand() {
        return getRequest(ServerCommand.class);
    }

    public boolean isInitatedBy(ServerCommand other) {
        return getRequestServerCommand().equals(other);
    }

    public <T> T getRequest(Class<T> classOfT) {
        return GsonMapperFactory.createGson().fromJson(responseBody.get(PROPERTY_REQUEST), classOfT);
    }

    public <T> T getReply(Class<T> classOfT) {
        String responseProperty = getRequestServerCommand().getCommand().getResponsePropertyName();
        return GsonMapperFactory.createGson().fromJson(responseBody.get(responseProperty), classOfT);
    }
}
