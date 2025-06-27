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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Deserializes a Json Element to {@link GiraOneCommand} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneCommandDeserializer implements JsonDeserializer<GiraOneCommand> {
    private final Set<Class<?>> giraOneCommandClasses;

    public GiraOneCommandDeserializer(@NonNull Set<Class<?>> commandClasses) {
        this.giraOneCommandClasses = commandClasses;
    }

    @Override
    @Nullable
    public GiraOneCommand deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonDeserializationContext != null && jsonElement != null && jsonElement.isJsonObject()) {
            String command = ((JsonObject) jsonElement).getAsJsonPrimitive("command").getAsString();
            Optional<Class<?>> commandClassOptional = giraOneCommandClasses.stream()
                    .filter(f -> command.equals(f.getAnnotation(GiraOneServerCommand.class).name())).findFirst();
            if (commandClassOptional.isPresent()) {
                return Objects.requireNonNull(jsonDeserializationContext).deserialize(jsonElement,
                        commandClassOptional.get());
            }
        }
        return new GiraOneCommand();
    }
}
