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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.commands.ServerCommand;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializes a {@link ServerCommand} into it's Json Representation.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneCommandRequestSerializer implements JsonSerializer<ServerCommand> {
    static final String PROPERTY_REQUEST = "request";

    @Override
    public JsonElement serialize(@Nullable ServerCommand serverCommand, @Nullable Type type,
            @Nullable JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        Map<String, ServerCommand> wrapper = new HashMap<>();
        if (serverCommand != null) {
            wrapper.put(PROPERTY_REQUEST, serverCommand);
        }
        return gson.toJsonTree(wrapper);
    }
}
