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

import com.google.common.reflect.TypeToken;
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
import java.util.ArrayList;
import java.util.Map;

/**
 * Deserializes a Json Element to {@link GiraOneChannel} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneChannelDeserializer extends GiraOneMessageJsonTypeAdapter implements JsonDeserializer<GiraOneChannel> {
    private final Type listTypeGiraOneDataPoint = new TypeToken<ArrayList<GiraOneDataPoint>>(){}.getType();

    @Override
    @Nullable
    public GiraOneChannel deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            assert jsonDeserializationContext != null;

            GiraOneChannel channel = new GiraOneChannel();
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                switch (entry.getKey()) {
                    case "channelViewId":
                        channel.setChannelViewId(entry.getValue().getAsInt());
                        break;
                    case "name":
                        channel.setName(entry.getValue().getAsString());
                        break;
                    case "urn", "channelViewUrn":
                        channel.setUrn(entry.getValue().getAsString());
                        break;
                    case "functionType":
                        channel.setFunctionType(jsonDeserializationContext.deserialize(entry.getValue(), GiraOneFunctionType.class));
                        break;
                    case "channelType":
                        channel.setChannelType(jsonDeserializationContext.deserialize(entry.getValue(), GiraOneChannelType.class));
                        break;
                    case "channelTypeId":
                        channel.setChannelTypeId(jsonDeserializationContext.deserialize(entry.getValue(), GiraOneChannelTypeId.class));
                        break;
                    case "datapoints", "dataPoints":
                        channel.setDataPoints(jsonDeserializationContext.deserialize(entry.getValue(), listTypeGiraOneDataPoint));
                    default:
                        break;
                }
            }
            return channel;
        }
        throw new JsonParseException("Cannot parse JsonElement as GiraOneChannel.");
    }
}
