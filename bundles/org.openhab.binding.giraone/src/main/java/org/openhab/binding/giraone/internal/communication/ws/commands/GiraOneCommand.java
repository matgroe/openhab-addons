package org.openhab.binding.giraone.internal.communication.ws.commands;

/**
 * This enumeration defines the command strings which might be
 * sent to the Gira One Server. The server answers the command
 * within as named by "responsePropertyName" within it's JSON
 * response body.
 */
public enum GiraOneCommand {
    /** First Command to send to server, otherwise no events are getting generated */
    RegisterApplication(null),

    /** Reads all Things and channels to be represented by any UI */
    GetUIConfiguration("config"),

    GetProcessView("processView");

    private final String responsePropertyName;

    GiraOneCommand(String responsePropertyName) {
        this.responsePropertyName = responsePropertyName;
    }

    public String getResponsePropertyName() {
        return responsePropertyName;
    }
}
