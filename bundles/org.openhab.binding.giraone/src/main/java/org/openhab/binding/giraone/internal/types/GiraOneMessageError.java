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

package org.openhab.binding.giraone.internal.types;

import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type for received error states from gira one server
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneMessageError {

    @SerializedName("text")
    private String error = "OK";

    @SerializedName("hint")
    private String hint = "";

    @SerializedName("code")
    private int code = 0;

    public String getHint() {
        return hint;
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }

    public boolean isErrorState() {
        return !"OK".equalsIgnoreCase(error);
    }

    @Override
    public String toString() {
        return String.format("GiraOneMessageError: %d -- %s (%s)", code, error, hint);
    }
}
