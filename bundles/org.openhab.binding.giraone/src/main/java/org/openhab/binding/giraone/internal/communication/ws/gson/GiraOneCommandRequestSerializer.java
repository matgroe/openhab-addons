package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;

import com.google.gson.*;

public class GiraOneCommandRequestSerializer implements JsonSerializer<ServerCommand> {
    final static String PROPERTY_REQUEST = "request";

    @Override
    public JsonElement serialize(ServerCommand serverCommand, Type type,
            JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        Map<String, ServerCommand> wrapper = new HashMap<>();
        wrapper.put(PROPERTY_REQUEST, serverCommand);
        return gson.toJsonTree(wrapper);
    }
}
