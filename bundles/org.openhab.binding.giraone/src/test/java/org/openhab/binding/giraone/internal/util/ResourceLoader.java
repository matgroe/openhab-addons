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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test utility class for loading local resources
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public abstract class ResourceLoader {

    public static String loadStringResource(final String name) {
        try (InputStream inputStream = ResourceLoader.class.getResourceAsStream(name)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
            throw new RuntimeException("Cannot create Inputstream for resource '" + name + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonObject loadJsonResource(final String name) {
        try (InputStream inputStream = ResourceLoader.class.getResourceAsStream(name)) {
            Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            return (JsonObject) JsonParser.parseReader(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
