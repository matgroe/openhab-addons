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
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneDataPoint} within context of Gson parsing.
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
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has(GiraOneJsonPropertyNames.PROPERTY_URN)) {
                    return new GiraOneDataPoint(jsonObject.get(GiraOneJsonPropertyNames.PROPERTY_URN).getAsString());
                }
                return new GiraOneDataPoint("urn:gds:dp:GiraOneServer:invalid:resource");
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Cannot parse JsonElement as GiraOneDataPoint.", e);
            }
        }
        throw new JsonParseException("Cannot parse empty JsonElement as GiraOneDataPoint.");
    }
}
