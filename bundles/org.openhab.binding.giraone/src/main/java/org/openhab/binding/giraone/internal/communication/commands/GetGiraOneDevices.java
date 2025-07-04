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
package org.openhab.binding.giraone.internal.communication.commands;

import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

import java.util.HashMap;

/**
 * {@link GiraOneCommand} for reading getting a gira one device configuration
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "GetGiraOneDevices", responsePayload = "devices")
public class GetGiraOneDevices extends GiraOneCommand {
    @SerializedName("object")
    private final HashMap<String, Object> object = new HashMap<>();

    public static GenericBuilder<GetGiraOneDevices> builder() {
        return GenericBuilder.of(GetGiraOneDevices::new);
    }

    protected GetGiraOneDevices() {
    }

    public void setId(int id) {
        this.object.put("id", id);
    }
}
