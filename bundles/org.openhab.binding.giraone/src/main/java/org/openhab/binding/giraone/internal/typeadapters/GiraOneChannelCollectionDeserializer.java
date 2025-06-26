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
import org.openhab.binding.giraone.internal.types.GiraOneProject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

/**
 * Deserializes a Json Element to {@link GiraOneProject} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneChannelCollectionDeserializer implements JsonDeserializer<GiraOneChannelCollection> {
    private static final String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    private static final String PROPERTY_CHANNEL_VIEW_URN = "channelViewUrn";
    private static final String PROPERTY_CONTENT = "content";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_MAINTYPE = "mainType";


    @Override
    @Nullable
    public GiraOneChannelCollection deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        assert jsonElement != null;
        assert jsonDeserializationContext != null;

        GiraOneChannelCollection channelCollection = new GiraOneChannelCollection();
        if (jsonElement.isJsonArray()) {

            List<JsonObject> list = jsonElement.getAsJsonArray().asList().stream().filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .filter(e -> e.has(PROPERTY_MAINTYPE) && "Root".equals(e.get(PROPERTY_MAINTYPE).getAsString()))
                    .findFirst().stream()
                    .map(this::streamJsonObjectOfGiraOneComponents)
                    .map(Stream::toList)
                    .flatMap(this::makeFlat)
                    .toList();

            jsonElement.getAsJsonArray().asList().stream().filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject).filter(e -> e.has(PROPERTY_CHANNEL_VIEW_URN))
                    .map(e -> enrichLocation(jsonDeserializationContext, e))
                    .map(e -> createGiraOneChannel(jsonDeserializationContext, e)).forEach(channelCollection::add);
        }
        return channelCollection;
    }

    private Stream<JsonObject> makeFlat(List<JsonElement> jsonElements) {
        return jsonElements.stream().map(JsonElement::getAsJsonObject);
    }


    private JsonObject enrichLocation(JsonDeserializationContext jsonDeserializationContext,
                                      JsonObject jsonObject) {
        return jsonObject;
    }

    private GiraOneChannel createGiraOneChannel(JsonDeserializationContext jsonDeserializationContext,
            JsonElement jsonElement) {
        return jsonDeserializationContext.deserialize(jsonElement, GiraOneChannel.class);
    }

    private Stream<JsonElement> streamJsonObjectOfGiraOneComponents(JsonObject jsonObject) {
        Stream<JsonElement> jsonElements = Stream.empty();
        if (jsonObject.has(PROPERTY_CONTENT)) {
            jsonElements = Stream.concat(jsonElements,
                    streamJsonArrayOfOfGiraOneComponents(jsonObject, jsonObject.getAsJsonArray(PROPERTY_CONTENT)));
        }
        return jsonElements;
    }

    private Stream<JsonElement> streamJsonArrayOfOfGiraOneComponents(JsonObject jsonParentObject, JsonArray jsonArray) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (JsonElement e : jsonArray.asList()) {
            if (e.isJsonObject()) {
                JsonObject jsonObject = e.getAsJsonObject();

                if (jsonObject.has(PROPERTY_MAINTYPE)) {
                    if (jsonObject.has(PROPERTY_NAME) && jsonParentObject.has(PROPERTY_NAME)) {
                        String parentName = jsonParentObject.get(PROPERTY_NAME).getAsString();
                        String name = jsonObject.get(PROPERTY_NAME).getAsString();
                        jsonObject.addProperty(PROPERTY_NAME, parentName + " > " + name);
                    }

                    jsonElements = Stream.concat(jsonElements, streamJsonObjectOfGiraOneComponents(jsonObject));
                } else if (jsonObject.has(PROPERTY_CHANNEL_VIEW_ID)) {
                    if (jsonParentObject.has(PROPERTY_NAME)) {
                        String parentName = jsonParentObject.get(PROPERTY_NAME).getAsString();
                        jsonObject.addProperty(PROPERTY_NAME, parentName);
                    }
                    jsonElements = Stream.concat(jsonElements, Stream.of(jsonObject));
                }
            }
        }
        return jsonElements;
    }
}
