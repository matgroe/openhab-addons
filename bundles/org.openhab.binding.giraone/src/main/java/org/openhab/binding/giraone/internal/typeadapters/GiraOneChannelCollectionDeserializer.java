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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneComponent;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Deserializes a Json Element to {@link GiraOneProject} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneChannelCollectionDeserializer implements JsonDeserializer<GiraOneChannelCollection> {
    @Override
    @Nullable
    public GiraOneChannelCollection deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
                                                  @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        assert jsonElement != null;
        GiraOneChannelCollection channelCollection  = new GiraOneChannelCollection();
        if (jsonElement.isJsonArray()) {
            jsonElement.getAsJsonArray().asList().stream()
                    .filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .filter(e -> e.has("channelViewUrn"))
                    .map(e -> createGiraOneChannel(jsonDeserializationContext, e))
                    .forEach(channelCollection::add);
        }
        return channelCollection;
    }

    private GiraOneChannel createGiraOneChannel(JsonDeserializationContext jsonDeserializationContext, JsonElement jsonElement) {
        return jsonDeserializationContext.deserialize(jsonElement, GiraOneChannel.class);
    }

}
