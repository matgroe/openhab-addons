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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes a Json Element to {@link GiraOneChannelType} within context of Gson parsing.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE })
public class GiraOneCommandDeserializer implements JsonDeserializer<GiraOneCommand> {
    private static final Set<Class<?>> giraOneCommandClasses = findAllGiraOneServerCommandClasses();

    private static Set<Class<?>> findAllGiraOneServerCommandClasses() {
        try {
            return ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
                    .filter(clazz -> clazz.getPackageName()
                            .equalsIgnoreCase("org.openhab.binding.giraone.internal.communication.commands"))
                    .map(clazz -> clazz.load()).filter(clazz -> clazz.isAnnotationPresent(GiraOneServerCommand.class))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    @Override
    @Nullable
    public GiraOneCommand deserialize(@Nullable JsonElement jsonElement, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement != null && jsonElement.isJsonObject()) {
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
