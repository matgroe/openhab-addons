package org.openhab.binding.giraone.internal.communication.ws.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneEvent;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;

public abstract class GsonMapperFactory {

    private GsonMapperFactory() { }

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(GiraOneMessageType.class, new GiraOneMessageTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneEvent.class, new GiraOneEventDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneCommandResponse.class, new GiraOneCommandResponseDeserializer());
        gsonBuilder.registerTypeAdapter(ServerCommand.class, new GiraOneCommandRequestSerializer());

        return gsonBuilder;
    }

    public static Gson createGson() {
        return createGsonBuilder().create();
    }
}
