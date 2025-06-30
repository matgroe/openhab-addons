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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.webservice.GiraOneWebserviceRequest;

import java.lang.reflect.Type;

/**
 * Serializes a {@link GiraOneWebserviceRequest} into it's Json Representation.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebserviceCommandRequestSerializer implements JsonSerializer<GiraOneWebserviceRequest> {
    private static final String PROPERTY_COMMAND_NAME = "command";
    private static final String PROPERTY_KEEP_ALIVE = "keepAlive";
    private static final String PROPERTY_DATA = "data";

    @Override
    public JsonElement serialize(@Nullable GiraOneWebserviceRequest serverCommand, @Nullable Type type,
            @Nullable JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        assert serverCommand != null;

        json.addProperty(PROPERTY_COMMAND_NAME, serverCommand.getCommand().getCommand());
        json.addProperty(PROPERTY_KEEP_ALIVE, true);
        if (jsonSerializationContext != null) {
            json.add(PROPERTY_DATA, jsonSerializationContext.serialize(serverCommand.getCommand()));
        }
        return json;
    }
}
