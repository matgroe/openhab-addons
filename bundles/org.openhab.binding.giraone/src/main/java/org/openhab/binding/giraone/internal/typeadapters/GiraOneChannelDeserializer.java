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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;

import java.lang.reflect.Type;
import java.util.Map;

import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_TYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_TYPE_ID;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_CHANNEL_VIEW_URN;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_DATAPOINTS;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_DATA_POINTS_CC;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_FUNCTION_TYPE;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_LOCATION;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_NAME;
import static org.openhab.binding.giraone.internal.typeadapters.GiraOneJsonPropertyNames.PROPERTY_URN;

/**
 * Deserializes a Json Element to {@link GiraOneChannel} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneChannelDeserializer extends GiraOneMessageJsonTypeAdapter
        implements JsonDeserializer<GiraOneChannel> {

    @Override
    @Nullable
    public GiraOneChannel deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            assert jsonDeserializationContext != null;

            GiraOneChannel channel = new GiraOneChannel();
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                switch (entry.getKey()) {
                    case PROPERTY_LOCATION:
                        channel.setLocation(entry.getValue().getAsString());
                        break;
                    case PROPERTY_NAME:
                        channel.setName(entry.getValue().getAsString());
                        break;
                    case PROPERTY_CHANNEL_VIEW_URN, PROPERTY_URN:
                        channel.setUrn(entry.getValue().getAsString());
                        break;
                    case PROPERTY_FUNCTION_TYPE:
                        channel.setFunctionType(
                                jsonDeserializationContext.deserialize(entry.getValue(), GiraOneFunctionType.class));
                        break;
                    case PROPERTY_CHANNEL_TYPE:
                        channel.setChannelType(
                                jsonDeserializationContext.deserialize(entry.getValue(), GiraOneChannelType.class));
                        break;
                    case PROPERTY_CHANNEL_TYPE_ID:
                        channel.setChannelTypeId(
                                jsonDeserializationContext.deserialize(entry.getValue(), GiraOneChannelTypeId.class));
                        break;
                    case PROPERTY_DATAPOINTS, PROPERTY_DATA_POINTS_CC:
                        addDatapoints(channel, jsonDeserializationContext, entry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return channel;
        }
        throw new JsonParseException("Cannot parse JsonElement as GiraOneChannel.");
    }

    private void addDatapoints(GiraOneChannel channel, @Nullable JsonDeserializationContext jsonDeserializationContext,
            JsonElement jsonElement) {
        if (jsonDeserializationContext != null && jsonElement.isJsonArray()) {
            jsonElement.getAsJsonArray().asList().forEach(
                    elem -> channel.addDataPoint(jsonDeserializationContext.deserialize(elem, GiraOneDataPoint.class)));
        }
    }
}
