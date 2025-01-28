package org.openhab.binding.giraone.internal.communication.ws.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;

import java.lang.reflect.Type;

class GiraOneCommandResponseDeserializer extends GiraOneMessageJsonTypeAdapter implements JsonDeserializer<GiraOneCommandResponse> {

    @Override
    public GiraOneCommandResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (isResponse(jsonElement)) {
            return new GiraOneCommandResponse(getResponse(jsonElement));
        }
        return null;
    }
}
