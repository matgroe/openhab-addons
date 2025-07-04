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
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneComponent;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneURN;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNELS;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_COMPONENTS;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_DATAPOINTS;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_FUNCTION_TYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_LOCATION;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_NAME;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_SUBLOCATIONS;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_TYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_URN;

/**
 * Deserializes a Json Element to {@link GiraOneComponentCollection} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneComponentCollectionDeserializer implements JsonDeserializer<GiraOneComponentCollection> {

    @Override
    @Nullable
    public GiraOneComponentCollection deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        GiraOneComponentCollection diagDevices = new GiraOneComponentCollection();
        if (jsonDeserializationContext != null && jsonElement != null && jsonElement.isJsonObject()) {
            streamJsonObjectOfGiraOneComponents(jsonElement.getAsJsonObject()).map(JsonElement::getAsJsonObject)
                    .map(e -> this.createGiraOneComponent(jsonDeserializationContext, e)).forEach(diagDevices::add);
        }
        return diagDevices;
    }

    private GiraOneComponent createGiraOneComponent(JsonDeserializationContext jsonDeserializationContext,
            JsonObject jsonObject) {
        GiraOneComponentType cmpType = jsonDeserializationContext.deserialize(jsonObject.get(PROPERTY_URN),
                GiraOneComponentType.class);
        if (cmpType == GiraOneComponentType.KnxButton) {
            addKnxButtonProperties(jsonObject);
        }
        jsonObject.add(PROPERTY_TYPE, jsonObject.get(PROPERTY_URN));
        return jsonDeserializationContext.deserialize(jsonObject, GiraOneComponent.class);
    }

    private void addKnxButtonProperties(JsonObject jsonObject) {
        for (JsonElement channelElement : jsonObject.getAsJsonArray(PROPERTY_CHANNELS)) {
            JsonObject channelObject = channelElement.getAsJsonObject();
            channelObject.addProperty(PROPERTY_FUNCTION_TYPE, GiraOneFunctionType.Trigger.getName());
            channelObject.addProperty(PROPERTY_CHANNEL_TYPE, GiraOneChannelType.Trigger.getName());
            channelObject.addProperty(PROPERTY_CHANNEL_TYPE_ID, GiraOneChannelTypeId.Button.getName());

            JsonArray datapoints = channelObject.getAsJsonArray(PROPERTY_DATAPOINTS);
            channelObject.addProperty(PROPERTY_URN, buildDatapointDeviceUrn(datapoints));

            String channelName = String.format(("%s, %s"), jsonObject.getAsJsonPrimitive(PROPERTY_NAME).getAsString(),
                    channelObject.getAsJsonPrimitive(PROPERTY_NAME).getAsString());
            channelObject.addProperty(PROPERTY_NAME, channelName);
        }
    }

    @Nullable
    private String buildDatapointDeviceUrn(JsonArray datapoints) {
        return datapoints.asList().stream().map(JsonElement::getAsJsonObject)
                .map(e -> e.get(PROPERTY_URN).getAsString()).map(GiraOneURN::of).map(GiraOneURN::getParent).distinct()
                .findFirst().orElse(GiraOneURN.INVALID).toString();
    }

    private Stream<JsonElement> streamJsonObjectOfGiraOneComponents(JsonObject jsonObject) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            switch (entry.getKey()) {
                case PROPERTY_COMPONENTS:
                    jsonElements = Stream.concat(jsonElements,
                            jsonObject.getAsJsonArray(entry.getKey()).asList().stream());
                case PROPERTY_SUBLOCATIONS:
                    jsonElements = Stream.concat(jsonElements,
                            streamJsonArrayOfSubLocation(jsonObject, entry.getValue().getAsJsonArray()));
                default:
                    break;
            }
        }
        return jsonElements;
    }

    private Stream<JsonElement> streamJsonArrayOfSubLocation(JsonObject parent, JsonArray jsonArray) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (JsonElement e : jsonArray.asList()) {
            if (e.isJsonObject()) {
                e.getAsJsonObject().add(PROPERTY_LOCATION, parent.get(PROPERTY_NAME));
                jsonElements = Stream.concat(jsonElements, streamJsonObjectOfGiraOneComponents(e.getAsJsonObject()));
            }
        }
        return jsonElements;
    }
}
