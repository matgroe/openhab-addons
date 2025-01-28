package org.openhab.binding.giraone.internal.communication.ws;

import com.google.gson.JsonObject;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;
import org.openhab.binding.giraone.internal.communication.ws.gson.GsonMapperFactory;

public class GiraOneCommandResponse {
    final static String PROPERTY_REQUEST = "request";
    final JsonObject responseBody;

    public GiraOneCommandResponse(final JsonObject responseBody) {
        this.responseBody = responseBody;
    }

     public ServerCommand getRequestServerCommand() {
        return getRequest(ServerCommand.class);
    }

    public <T> T getRequest(Class<T> classOfT) {
        return GsonMapperFactory.createGson().fromJson(responseBody.get(PROPERTY_REQUEST), classOfT);
    }

    public <T> T getReply(Class<T> classOfT) {
        String responseProperty = getRequestServerCommand().getCommand().getResponsePropertyName();
        return GsonMapperFactory.createGson().fromJson(responseBody.get(responseProperty), classOfT);
    }
}
