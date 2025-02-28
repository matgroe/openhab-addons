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
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneChannelTypeIdDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneCommandRequestSerializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneCommandResponseDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneEventDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneFunctionTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneItemMainTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneItemSubTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneMessageTypeDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneProjectDeserializer;
import org.openhab.binding.giraone.internal.typeadapters.GiraOneProjectItemDeserializer;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneEvent;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneItemMainType;
import org.openhab.binding.giraone.internal.types.GiraOneItemSubType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.types.GiraOneProjectItem;

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
        gsonBuilder.registerTypeAdapter(GiraOneCommandResponse.class, new GiraOneCommandResponseDeserializer());
        gsonBuilder.registerTypeAdapter(ServerCommand.class, new GiraOneCommandRequestSerializer());
        gsonBuilder.registerTypeAdapter(GiraOneProject.class, new GiraOneProjectDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneProjectItem.class, new GiraOneProjectItemDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneItemMainType.class, new GiraOneItemMainTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneItemSubType.class, new GiraOneItemSubTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelTypeId.class, new GiraOneChannelTypeIdDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelType.class, new GiraOneChannelTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneFunctionType.class, new GiraOneFunctionTypeDeserializer());
        return gsonBuilder;
    }

    public static Gson createGson() {
        return createGsonBuilder().create();
    }
}
