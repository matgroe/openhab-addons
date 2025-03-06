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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneItemSubType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneItemSubType} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneItemSubTypeDeserializer implements JsonDeserializer<GiraOneItemSubType> {
    private final Logger logger = LoggerFactory.getLogger(GiraOneItemSubTypeDeserializer.class);

    @Override
    @Nullable
    public GiraOneItemSubType deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null) {
            try {
                return GiraOneItemSubType.valueOf(jsonElement.getAsString());
            } catch (IllegalArgumentException exp) {
                logger.warn("Cannot map '{}' into enum of {}", jsonElement.getAsString(),
                        GiraOneItemSubType.class.getName());
            }
        }
        return GiraOneItemSubType.Unknown;
    }
}
