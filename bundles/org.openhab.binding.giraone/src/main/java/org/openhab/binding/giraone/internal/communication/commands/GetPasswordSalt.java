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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneServerCommand;
import org.openhab.binding.giraone.internal.util.GenericBuilder;

import com.google.gson.annotations.SerializedName;

/**
 * {@link GiraOneCommand} for Webservice Command for creating password salt.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
@GiraOneServerCommand(name = "getPasswordSalt", responsePayload = "data")
public class GetPasswordSalt extends GiraOneCommand {
    @SerializedName("username")
    private String username = "";

    public static GenericBuilder<GetPasswordSalt> builder() {
        return GenericBuilder.of(GetPasswordSalt::new);
    }

    private GetPasswordSalt() {
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
