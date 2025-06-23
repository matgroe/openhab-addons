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
package org.openhab.binding.giraone.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.webservice.GiraOneWebserviceRequest;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketResponse;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelCollectionDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelTypeIdDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneCommandDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneComponentCollectionDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneComponentTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneDataPointDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneEventDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneFunctionTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneMessageTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneWebserviceCommandRequestSerializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneWebsocketResponseDeserializer;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.types.GiraOneComponentType;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class offers creation functions for a pre-configured {@link GsonBuilder}
 * that references all required {@link com.google.gson.JsonDeserializer} instances
 * and for the concerning {@link Gson} object as well.
 * *
 * 
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public abstract class GsonMapperFactory {
    private GsonMapperFactory() {
    }

    /**
     * @return pre-configured {@link GsonBuilder}that references all
     *         required {@link com.google.gson.JsonDeserializer} instances within
     *         the giraone domain.
     */
    public static GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(GiraOneMessageType.class, new GiraOneMessageTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneEvent.class, new GiraOneEventDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneWebsocketResponse.class, new GiraOneWebsocketResponseDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneWebserviceRequest.class,
                new GiraOneWebserviceCommandRequestSerializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannel.class, new GiraOneChannelDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneDataPoint.class, new GiraOneDataPointDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelTypeId.class, new GiraOneChannelTypeIdDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelType.class, new GiraOneChannelTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneFunctionType.class, new GiraOneFunctionTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneCommand.class, new GiraOneCommandDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneComponentCollection.class, new GiraOneComponentCollectionDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneComponentType.class, new GiraOneComponentTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelCollection.class, new GiraOneChannelCollectionDeserializer());
        return gsonBuilder;
    }

    public static Gson createGson() {
        return createGsonBuilder().create();
    }
}
