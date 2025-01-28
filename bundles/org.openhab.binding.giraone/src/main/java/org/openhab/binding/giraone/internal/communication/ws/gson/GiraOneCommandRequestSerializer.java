package org.openhab.binding.giraone.internal.communication.ws.gson;

import com.google.gson.*;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GiraOneCommandRequestSerializer implements JsonSerializer<ServerCommand>{
    static long COMMAND_COUNTER = 0L;
    final static String PROPERTY_REQUEST = "request";

    @Override
    public JsonElement serialize(ServerCommand serverCommand, Type type, JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        serverCommand.setCommandId( ++GiraOneCommandRequestSerializer.COMMAND_COUNTER);
        Map<String, ServerCommand> wrapper = new HashMap<>();
        wrapper.put(PROPERTY_REQUEST, serverCommand);
        return gson.toJsonTree(wrapper);
    }
}
