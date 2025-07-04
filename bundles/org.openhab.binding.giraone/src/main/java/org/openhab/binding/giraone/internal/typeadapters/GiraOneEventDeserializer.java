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

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;

import java.lang.reflect.Type;

/**
 * Deserializes a Json Element to {@link GiraOneEvent} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneEventDeserializer extends GiraOneMessageJsonTypeAdapter implements JsonDeserializer<GiraOneEvent> {

    private JsonObject getValue(JsonElement jsonElement) {
        JsonObject event = getEvent(jsonElement);
        if (event.has("value")) {
            return event.getAsJsonObject("value");
        }
        return new JsonObject();
    }

    @Override
    @Nullable
    public GiraOneEvent deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && isEvent(jsonElement)) {
            JsonObject value = getValue(jsonElement);
            return new Gson().fromJson(value, GiraOneEvent.class);
        }
        throw new JsonParseException("Cannot parse JsonElement as GiraOneEvent.");
    }
}
