package org.openhab.binding.giraone.internal.communication.ws.gson;

import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneEvent;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;
import org.openhab.binding.giraone.internal.model.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonMapperFactory {

    private GsonMapperFactory() {
    }

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(GiraOneMessageType.class, new GiraOneMessageTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneEvent.class, new GiraOneEventDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneCommandResponse.class, new GiraOneCommandResponseDeserializer());
        gsonBuilder.registerTypeAdapter(ServerCommand.class, new GiraOneCommandRequestSerializer());
        gsonBuilder.registerTypeAdapter(GiraOneProject.class, new GiraOneProjectDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneProjectItem.class, new GiraOneProjectItemDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneItemMainType.class, new GiraOneItemMainTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneItemSubType.class, new GiraOneItemSubTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelTypeId.class, new GiraOneChannelTypeIdDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneChannelType.class, new GiraOneChannelTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GiraOneFunctionType.class, new GiraOneFunctionTypeDeserializer());
        return gsonBuilder;
    }

    public static Gson createGson() {
        return createGsonBuilder().create();
    }
}
