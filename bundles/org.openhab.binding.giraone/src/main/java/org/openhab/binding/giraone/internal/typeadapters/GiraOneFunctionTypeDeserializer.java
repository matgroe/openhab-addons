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
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Deserializes a Json Element to {@link GiraOneFunctionType} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneFunctionTypeDeserializer implements JsonDeserializer<GiraOneFunctionType> {
    private final Logger logger = LoggerFactory.getLogger(GiraOneFunctionTypeDeserializer.class);

    @Override
    @Nullable
    public GiraOneFunctionType deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null) {
            try {
                return GiraOneFunctionType.fromName(jsonElement.getAsString());
            } catch (IllegalArgumentException exp) {
                logger.warn("Cannot map '{}' into enum of {}", jsonElement.getAsString(),
                        GiraOneFunctionType.class.getName());
            }
        }
        return GiraOneFunctionType.Unknown;
    }
}
