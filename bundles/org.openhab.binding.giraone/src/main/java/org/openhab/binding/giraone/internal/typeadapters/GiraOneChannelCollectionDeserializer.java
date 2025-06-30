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

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_VIEW_ID;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_VIEW_URN;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CONTENT;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_LOCATION;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_MAINTYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_NAME;

/**
 * Uses the Gson parsing to deserializes the response of
 * {@link org.openhab.binding.giraone.internal.communication.GiraOneCommand} command
 * {@link org.openhab.binding.giraone.internal.communication.commands.GetDiagnosticDeviceList} into
 * {@link GiraOneChannelCollection}.
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
        assert jsonDeserializationContext != null;

        GiraOneChannelCollection channelCollection = new GiraOneChannelCollection();
        if (jsonElement.isJsonArray()) {
            // extract the channel -> location mapping from json
            List<JsonObject> channelLocations = jsonElement.getAsJsonArray().asList().stream()
                    .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                    .filter(e -> e.has(PROPERTY_MAINTYPE) && "Root".equals(e.get(PROPERTY_MAINTYPE).getAsString()))
                    .findFirst().stream().map(this::streamChannelLocationInformation).map(Stream::toList)
                    .flatMap(this::makeFlat).toList();

            // and convert into GiraOneChannel objects
            jsonElement.getAsJsonArray().asList().stream().filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject).filter(e -> e.has(PROPERTY_CHANNEL_VIEW_URN))
                    .map(e -> enrichLocation(jsonDeserializationContext, e, channelLocations))
                    .map(e -> createGiraOneChannel(jsonDeserializationContext, e)).forEach(channelCollection::add);
        }
        return channelCollection;
    }

    private Stream<JsonObject> makeFlat(List<JsonElement> jsonElements) {
        return jsonElements.stream().map(JsonElement::getAsJsonObject);
    }

    private JsonObject enrichLocation(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject,
            List<JsonObject> channelLocations) {
        JsonElement viewId = jsonObject.get(PROPERTY_CHANNEL_VIEW_ID);
        if (viewId != null) {
            channelLocations.stream().filter(f -> f.has(PROPERTY_CHANNEL_VIEW_ID))
                    .filter(f -> f.get(PROPERTY_CHANNEL_VIEW_ID).getAsString().equals(viewId.getAsString())).findFirst()
                    .ifPresent(object -> jsonObject.add(PROPERTY_LOCATION, object.get(PROPERTY_LOCATION)));
        }
        return jsonObject;
    }

    private GiraOneChannel createGiraOneChannel(JsonDeserializationContext jsonDeserializationContext,
            JsonElement jsonElement) {
        return jsonDeserializationContext.deserialize(jsonElement, GiraOneChannel.class);
    }

    private Stream<JsonElement> streamChannelLocationInformation(JsonObject jsonObject) {
        Stream<JsonElement> jsonElements = Stream.empty();
        if (jsonObject.has(PROPERTY_CONTENT)) {
            jsonElements = Stream.concat(jsonElements,
                    streamChannelLocationInformation(jsonObject, jsonObject.getAsJsonArray(PROPERTY_CONTENT)));
        }
        return jsonElements;
    }

    private Stream<JsonElement> streamChannelLocationInformation(JsonObject jsonParentObject, JsonArray jsonArray) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (JsonElement e : jsonArray.asList()) {
            if (e.isJsonObject()) {
                JsonObject jsonObject = e.getAsJsonObject();
                if (jsonObject.has(PROPERTY_MAINTYPE)) {
                    jsonElements = Stream.concat(jsonElements, streamChannelLocationInformation(jsonObject));
                } else if (jsonObject.has(PROPERTY_CHANNEL_VIEW_ID)) {
                    if (jsonParentObject.has(PROPERTY_NAME)) {
                        jsonObject.addProperty(PROPERTY_LOCATION, jsonParentObject.get(PROPERTY_NAME).getAsString());
                    }
                    jsonElements = Stream.concat(jsonElements, Stream.of(jsonObject));
                }
            }
        }
        return jsonElements;
    }
}
