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

import org.eclipse.jdt.annotation.NonNullByDefault;

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
            throw new ResourceException("Cannot create Inputstream for resource '" + name + "'");
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }
}
