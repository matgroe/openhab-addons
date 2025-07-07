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
import org.openhab.binding.giraone.internal.types.GiraOneValue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneValue} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneValueDeserializer extends GiraOneMessageJsonTypeAdapter implements JsonDeserializer<GiraOneValue> {

    @Nullable
    private JsonObject getValueAsJsonObject(@Nullable JsonElement jsonElement) {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("valueState") && "Value".equals(jsonObject.get("valueState").getAsString())) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    @Nullable
    public GiraOneValue deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = getValueAsJsonObject(jsonElement);
        if (jsonObject != null) {
            return new GiraOneValue(jsonObject.get("urn").getAsString(), jsonObject.get("value").getAsString());
        }
        throw new JsonParseException("Cannot parse JsonElement as GiraOneValue.");
    }
}
