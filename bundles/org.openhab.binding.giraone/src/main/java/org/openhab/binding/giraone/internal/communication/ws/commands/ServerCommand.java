package org.openhab.binding.giraone.internal.communication.ws.commands;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the command message to be sent out to Gira One Server.
 * It contains the {@link GiraOneCommand}, which defines the command
 * name and the property name within the command response json.
 * The unique commandId is built from command.name and some timestamp
 * information to map the received response to the requested server command.
 */
public class ServerCommand {
    @SerializedName(value = "_gdsqueryId")
    private String commandId;
    private final GiraOneCommand command;

    protected ServerCommand(GiraOneCommand command) {
        this.command = command;
        this.commandId = UUID.nameUUIDFromBytes(
                (command.name() + System.currentTimeMillis() + System.nanoTime()).getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    public GiraOneCommand getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServerCommand that))
            return false;
        return Objects.equals(commandId, that.commandId) && command == that.command;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, command);
    }
}
