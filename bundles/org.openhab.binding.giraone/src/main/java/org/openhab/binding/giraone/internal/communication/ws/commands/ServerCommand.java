package org.openhab.binding.giraone.internal.communication.ws.commands;

import com.google.gson.annotations.SerializedName;

public class ServerCommand {
    @SerializedName(value = "_gdsqueryId")
    private long commandId;

    private final GiraOneCommand command;

    protected ServerCommand(GiraOneCommand command) {
        this.command = command;
    }

    public GiraOneCommand getCommand() {
        return command;
    }

    public void setCommandId(long commandId) {
        this.commandId = commandId;
    }
}
