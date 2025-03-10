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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannelViewRef;
import org.openhab.binding.giraone.internal.types.GiraOneItemMainType;
import org.openhab.binding.giraone.internal.types.GiraOneItemReference;
import org.openhab.binding.giraone.internal.types.GiraOneItemSubType;
import org.openhab.binding.giraone.internal.types.GiraOneProjectItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneProjectItem} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneProjectItemDeserializer implements JsonDeserializer<GiraOneProjectItem> {
    private static final String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    private static final String PROPERTY_MAIN_TYPE = "mainType";
    private static final String PROPERTY_SUB_TYPE = "subType";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_URN = "urn";
    private static final String PROPERTY_CONTENT = "content";

    @Override
    @Nullable
    public GiraOneProjectItem deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonObject()) {
            throw new JsonParseException("JsonObject expected here.");
        }
        JsonObject json = jsonElement.getAsJsonObject();
        if (isProjectItem(json)) {
            GiraOneProjectItem item = new GiraOneProjectItem();

            item.setMainType(
                    jsonDeserializationContext.deserialize(json.get(PROPERTY_MAIN_TYPE), GiraOneItemMainType.class));
            item.setSubType(
                    jsonDeserializationContext.deserialize(json.get(PROPERTY_SUB_TYPE), GiraOneItemSubType.class));
            item.setName(json.get(PROPERTY_NAME).getAsString());
            item.setUrn(json.get(PROPERTY_URN).getAsString());

            if (hasProjectItemContentArray(json)) {
                JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
                GiraOneProjectItem[] items = jsonDeserializationContext.deserialize(contentArray,
                        GiraOneProjectItem[].class);
                item.setChildren(Arrays.stream(items).toList());
            } else if (hasChannelRefContentArray(json)) {
                JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
                GiraOneItemReference[] items = jsonDeserializationContext.deserialize(contentArray,
                        GiraOneChannelViewRef[].class);
                item.setItemReferences(Arrays.stream(items).toList());
            }
            return item;
        }

        throw new JsonParseException("The JsonElement is not parseable as GiraOneProjectItem.");
    }

    private boolean isProjectItem(JsonObject json) {
        return json.has(PROPERTY_MAIN_TYPE) && json.has(PROPERTY_SUB_TYPE) && json.has(PROPERTY_NAME)
                && json.has(PROPERTY_URN);
    }

    private boolean hasChannelRefContentArray(JsonObject json) {
        if (json.has(PROPERTY_CONTENT) && json.get(PROPERTY_CONTENT).isJsonArray()) {
            JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
            if (!contentArray.isEmpty()) {
                return isChannelRef(contentArray.get(0).getAsJsonObject());
            }
        }
        return false;
    }

    private boolean isChannelRef(JsonObject json) {
        return json.has(PROPERTY_CHANNEL_VIEW_ID);
    }

    private boolean hasProjectItemContentArray(JsonObject json) {
        if (json.has(PROPERTY_CONTENT) && json.get(PROPERTY_CONTENT).isJsonArray()) {
            JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
            if (!contentArray.isEmpty()) {
                return isProjectItem(contentArray.get(0).getAsJsonObject());
            }
        }
        return false;
    }
}
