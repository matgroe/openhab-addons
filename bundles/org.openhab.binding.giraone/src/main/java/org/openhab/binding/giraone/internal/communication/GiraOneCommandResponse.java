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
package org.openhab.binding.giraone.internal.communication;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;

/**
 * This interface represents a command response as received from the Gira One Sever.
 * It offers access to the raw {@link JsonObject} and the deserialized Object as well.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public interface GiraOneCommandResponse {

    /**
     * @return returns the raw {@link JsonObject} as received from Gira One Server.
     */
    JsonObject getResponseBody();

    /**
     * @param <T> The typed response
     * @param classOfT The class, ths response should get deserializes into
     *
     * @return The deserialized response
     */
    default <T> T getReply(Class<T> classOfT) {
        return GsonMapperFactory.createGson().fromJson(getResponseBody(), classOfT);
    }

    /**
     * @param <T> The typed response
     * @param typeOfT The Type<T>, this response should get deserializes into
     *
     * @return The deserialized response
     */
    default <T> T getReply(Type typeOfT) {
        return GsonMapperFactory.createGson().fromJson(getResponseBody(), typeOfT);
    }
}
