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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the command message to be sent out to Gira One Server.
 * It contains the {@link GiraOneCommand}, which defines the command
 * name and the property name within the command response json.
 * The unique commandId is built from command.name and some timestamp
 * information to map the received response to the requested server command.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class ServerCommand {
    @SerializedName(value = "_gdsqueryId")
    private Integer commandId;

    private final GiraOneCommand command;

    protected ServerCommand(GiraOneCommand command) {
        this.command = command;
        this.commandId = ServerCommandSequence.generate();
    }

    public GiraOneCommand getCommand() {
        return command;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof ServerCommand that)) {
            return false;
        }
        return Objects.equals(commandId, that.commandId) && command == that.command;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, command);
    }
}
