package org.openhab.binding.giraone.internal.communication.webservice;

import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class GiraOneWebserviceRequest {
    @SerializedName(value = "data")
    private final JsonObject data;
    private final transient GiraOneCommand command;

    protected GiraOneWebserviceRequest(GiraOneCommand command) {
        data = (JsonObject) GsonMapperFactory.createGson().toJsonTree(command);
        this.command = command;
    }

    public GiraOneCommand getCommand() {
        return this.command;
    }
}
