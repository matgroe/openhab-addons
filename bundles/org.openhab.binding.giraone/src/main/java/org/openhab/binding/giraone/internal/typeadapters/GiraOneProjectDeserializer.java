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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneProjectItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneProject} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneProjectDeserializer implements JsonDeserializer<GiraOneProject> {
    private static final String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    private static final String PROPERTY_CHANNEL_VIEW_URN = "channelViewUrn";
    private static final String PROPERTY_CHANNEL_TYPE = "channelType";
    private static final String PROPERTY_CHANNEL_TYPE_ID = "channelTypeId";

    private static final String CONTENT_ROOT_MAIN_TYPE = "Root";
    private static final String CONTENT_ROOT_SUB_TYPE = CONTENT_ROOT_MAIN_TYPE;
    private static final String PROPERTY_CONTENT_MAIN_TYPE = "mainType";
    private static final String PROPERTY_CONTENT_SUB_TYPE = "subType";

    @Override
    @Nullable
    public GiraOneProject deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement == null || jsonDeserializationContext == null || !jsonElement.isJsonArray()) {
            throw new JsonParseException("JsonArray expected here.");
        }

        JsonArray content = jsonElement.getAsJsonArray();
        Optional<JsonElement> jsonRoot = content.asList().stream().filter(this::isProjectContentRootObject).findFirst();
        if (jsonRoot.isPresent()) {
            GiraOneProjectItem root = Objects
                    .requireNonNull(jsonDeserializationContext.deserialize(jsonRoot.get(), GiraOneProjectItem.class));

            List<GiraOneChannel> channels = content.asList().stream().filter(this::isProjectChannelObject)
                    .map(f -> makeChannel(f, Objects.requireNonNull(jsonDeserializationContext)))
                    .collect(Collectors.toList());

            return new GiraOneProject(root, channels);
        }
        throw new JsonParseException("Cannot parse received JsonArray to GiraOneProject.");
    }

    private boolean isProjectContentRootObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has(PROPERTY_CONTENT_MAIN_TYPE) && jsonObject.has(PROPERTY_CONTENT_SUB_TYPE)) {
                return CONTENT_ROOT_MAIN_TYPE.equals(jsonObject.get(PROPERTY_CONTENT_MAIN_TYPE).getAsString())
                        && CONTENT_ROOT_SUB_TYPE.equals(jsonObject.get(PROPERTY_CONTENT_SUB_TYPE).getAsString());
            }
        }
        return false;
    }

    private boolean isProjectChannelObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(PROPERTY_CHANNEL_VIEW_ID) && jsonObject.has(PROPERTY_CHANNEL_VIEW_URN)
                    && jsonObject.has(PROPERTY_CHANNEL_TYPE) && jsonObject.has(PROPERTY_CHANNEL_TYPE_ID);
        }
        return false;
    }

    private GiraOneChannel makeChannel(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
        return jsonDeserializationContext.deserialize(jsonElement, GiraOneChannel.class);
    }
}
