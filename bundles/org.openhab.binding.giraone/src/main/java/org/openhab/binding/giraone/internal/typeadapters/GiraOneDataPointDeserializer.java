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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneEvent} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneDataPointDeserializer extends GiraOneMessageJsonTypeAdapter
        implements JsonDeserializer<GiraOneDataPoint> {

    @Override
    @Nullable
    public GiraOneDataPoint deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            GiraOneDataPoint dataPoint = new GiraOneDataPoint();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                switch (entry.getKey()) {
                    case "dataPoint":
                    case "name":
                        dataPoint.setName(entry.getValue().getAsString());
                        break;
                    case "id":
                        dataPoint.setId(entry.getValue().getAsInt());
                        break;
                    case "urn":
                        dataPoint.setUrn(entry.getValue().getAsString());
                        break;
                    default:
                        break;
                }
            }
            return dataPoint;
        }
        throw new JsonParseException("Cannot parse JsonElement as GiraOneDataPoint.");
    }
}
