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
package org.openhab.binding.giraone.internal.communication.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * {@link GiraOneCommandResponse} implementation for responses as received from the
 * Gira One Webservice interface.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneWebserviceResponse implements GiraOneCommandResponse {
    @SerializedName(value = "data")
    public final JsonObject responseBody;

    public GiraOneWebserviceResponse(final JsonObject responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public JsonObject getResponseBody() {
        return responseBody;
    }
}
