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
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Deserializes a Json Element to {@link GiraOneProject} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneComponentCollectionDeserializer implements JsonDeserializer<GiraOneComponentCollection> {
    private static final String PROPERTY_SUBLOCATIONS = "subLocations";
    private static final String PROPERTY_COMPONENTS = "components";

    @Override
    @Nullable
    public GiraOneComponentCollection deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
                                                  @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        assert jsonElement != null;
        GiraOneComponentCollection diagDevices  = new GiraOneComponentCollection();

        if (jsonElement.isJsonObject()) {
            streamJsonObjectOfGiraOneComponents(jsonElement.getAsJsonObject())
                    .map(JsonElement::getAsJsonObject)
                    .map(e -> this.createGiraOneComponent(jsonDeserializationContext, e))
                            .forEach(diagDevices::add);
        }

        return diagDevices;
    }


    private GiraOneComponent createGiraOneComponent(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
        GiraOneComponentType cmpType = jsonDeserializationContext.deserialize(jsonObject.get("urn"), GiraOneComponentType.class);
        if (cmpType == GiraOneComponentType.KnxButton) {
            addKnxButtonProperties(jsonObject);
        }
        jsonObject.add("type", jsonObject.get("urn"));
        return jsonDeserializationContext.deserialize(jsonObject, GiraOneComponent.class);
    }

    private void addKnxButtonProperties(JsonObject jsonObject) {
        for (JsonElement channelElement : jsonObject.getAsJsonArray("channels")) {
            JsonObject channelObject = channelElement.getAsJsonObject();

            JsonArray datapoints = channelObject.getAsJsonArray("datapoints");
            String channelUrn = buildChannelUrnFromDatapoints(jsonObject.get("urn").getAsString(), datapoints);

            String channelName = String.format(("%s, %s"), jsonObject.getAsJsonPrimitive("name").getAsString(), channelObject.getAsJsonPrimitive("name").getAsString() );

            channelObject.addProperty("name", channelName);
            channelObject.addProperty("urn", channelUrn);
            channelObject.addProperty("functionType", GiraOneFunctionType.KnxButton.getName());
            channelObject.addProperty("channelType", deriveGiraOneChannelType(channelUrn).getName());
            channelObject.addProperty("channelTypeId", deriveGiraOneChannelTypeId(channelUrn).getName());
        }
    }

    private GiraOneChannelType deriveGiraOneChannelType(String channelUrn) {
        if (channelUrn.contains("Dimming") || channelUrn.contains("Switching")) {
            return GiraOneChannelType.Switch;
        }  else if (channelUrn.contains("Curtain")) {
            return GiraOneChannelType.Shutter;
        } else if (channelUrn.contains("Scene")) {
            return GiraOneChannelType.Function;
        }
        return GiraOneChannelType.Unknown;
    }

    private GiraOneChannelTypeId deriveGiraOneChannelTypeId(String channelUrn) {
        return GiraOneChannelTypeId.Unknown;
    }

    private String buildChannelUrnFromDatapoints(String componentUrn, JsonArray datapoints) {
        String[] parts = componentUrn.split(":");
        parts[parts.length - 1] = datapoints.asList().stream()
                .map(e -> e.getAsJsonObject().get("urn").getAsString())
                .map(this::extractChannelName)
                .distinct().findFirst().orElse(UUID.randomUUID().toString());
        return String.join(":", parts);
    }

    private String extractChannelName(String urn) {
        String[] urnParts = urn.split(":");
        return urnParts[urnParts.length -2];
    }

    private Stream<JsonElement> streamJsonObjectOfGiraOneComponents(JsonObject jsonObject) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            switch (entry.getKey()) {
                case PROPERTY_COMPONENTS:
                    jsonElements = Stream.concat(jsonElements, jsonObject.getAsJsonArray(entry.getKey()).asList().stream());
                case PROPERTY_SUBLOCATIONS:
                    jsonElements = Stream.concat(jsonElements, streamJsonArrayOfOfGiraOneComponents(entry.getValue().getAsJsonArray()));
                default:
                    break;
            }
        }
        return jsonElements;
    }

    private Stream<JsonElement> streamJsonArrayOfOfGiraOneComponents(JsonArray jsonArray) {
        Stream<JsonElement> jsonElements = Stream.empty();
        for (JsonElement e : jsonArray.asList()) {
            if (e.isJsonObject()) {
                jsonElements = Stream.concat(jsonElements, streamJsonObjectOfGiraOneComponents(e.getAsJsonObject()));
            }
        }
        return  jsonElements;
    }

}
