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

/**
 * {@link GiraOneCommand} Reads the gira one server device configuration
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "GetDeviceConfig", responsePayload = "deviceConfig")
public class GetDeviceConfig extends GiraOneCommand {

    @SerializedName("ipc")
    private final String ipc = "true";

    public static GenericBuilder<GetDeviceConfig> builder() {
        return GenericBuilder.of(GetDeviceConfig::new);
    }

    protected GetDeviceConfig() {
    }
}
