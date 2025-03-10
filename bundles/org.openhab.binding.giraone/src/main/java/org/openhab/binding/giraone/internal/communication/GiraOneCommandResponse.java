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

import java.util.Objects;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;
import org.openhab.binding.giraone.internal.types.GiraOneMessageError;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;

/**
 * This class represents a command response as received from the Gira One Sever
 * as to an received {@link ServerCommand}.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneCommandResponse {
    static final String PROPERTY_REQUEST = "request";
    static final String PROPERTY_ERROR = "error";

    final JsonObject responseBody;

    public GiraOneCommandResponse(final JsonObject responseBody) {
        this.responseBody = responseBody;
    }

    public ServerCommand getRequestServerCommand() {
        return Objects.requireNonNull(getRequest(ServerCommand.class), "Should not be null at any time");
    }

    public boolean isInitiatedBy(ServerCommand other) {
        return getRequestServerCommand().equals(other);
    }

    public <T> T getRequest(Class<T> classOfT) {
        return GsonMapperFactory.createGson().fromJson(responseBody.get(PROPERTY_REQUEST), classOfT);
    }

    public GiraOneMessageError getGiraMessageError() {
        return Objects.requireNonNullElse(
                GsonMapperFactory.createGson().fromJson(responseBody.get(PROPERTY_ERROR), GiraOneMessageError.class),
                new GiraOneMessageError());
    }

    public <T> T getReply(Class<T> classOfT) {
        String responseProperty = getRequestServerCommand().getCommand().getResponsePropertyName();
        if (responseProperty.isEmpty()) {
            return GsonMapperFactory.createGson().fromJson(responseBody, classOfT);
        } else {
            return GsonMapperFactory.createGson().fromJson(responseBody.get(responseProperty), classOfT);
        }
    }
}
