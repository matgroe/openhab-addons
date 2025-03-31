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
package org.openhab.binding.giraone.internal.typeadapters;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Base class offers some functionalities for deserializing reseived messages
 * from Gira One Server.
 * *
 * 
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
class GiraOneMessageJsonTypeAdapter {
    protected static final String PROPERTY_RESPONSE = "response";
    protected static final String PROPERTY_EVENT = "event";
    protected static final String PROPERTY_ERROR = "error";

    boolean isResponse(JsonElement jsonElement) {
        return isResponse(jsonElement.getAsJsonObject());
    }

    boolean isResponse(JsonObject jsonObject) {
        return jsonObject.has(PROPERTY_RESPONSE);
    }

    JsonObject getResponse(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(PROPERTY_RESPONSE).getAsJsonObject();
    }

    boolean isEvent(JsonElement jsonElement) {
        return isEvent(jsonElement.getAsJsonObject());
    }

    boolean isEvent(JsonObject jsonObject) {
        return jsonObject.has(PROPERTY_EVENT);
    }

    JsonObject getEvent(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(PROPERTY_EVENT).getAsJsonObject();
    }
}
