package org.openhab.binding.giraone.internal.communication.ws.gson;

import com.google.gson.*;
import io.swagger.v3.core.util.Json;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneEvent;

import java.lang.reflect.Type;

class GiraOneEventDeserializer extends GiraOneMessageJsonTypeAdapter implements JsonDeserializer<GiraOneEvent> {

    private JsonObject getValue(JsonElement jsonElement) {
        JsonObject event = getEvent(jsonElement);
        if (event.has("value")) {
            return event.getAsJsonObject("value");
        }
        return new JsonObject();
    }

    @Override
    public GiraOneEvent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (isEvent(jsonElement)) {
            JsonObject value = getValue(jsonElement);
            return new Gson().fromJson(value, GiraOneEvent.class);
        }
        return null;
    }
}
