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

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.types.GiraOneMessageError;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneMessageType} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneMessageTypeDeserializer extends GiraOneMessageJsonTypeAdapter
        implements JsonDeserializer<GiraOneMessageType> {

    @Override
    @Nullable
    public GiraOneMessageType deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && jsonDeserializationContext != null) {
            if (isResponse(jsonElement)) {
                return isError(getResponse(jsonElement), jsonDeserializationContext) ? GiraOneMessageType.Error
                        : GiraOneMessageType.Response;
            } else if (isEvent(jsonElement)) {
                return isError(getEvent(jsonElement), jsonDeserializationContext) ? GiraOneMessageType.Error
                        : GiraOneMessageType.Event;
            }
        }
        return GiraOneMessageType.Invalid;
    }

    private boolean isError(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        GiraOneMessageError error = jsonDeserializationContext.deserialize(jsonObject.getAsJsonObject(PROPERTY_ERROR),
                GiraOneMessageError.class);
        if (error != null) {
            return error.isErrorState();
        }
        return false;
    }
}
