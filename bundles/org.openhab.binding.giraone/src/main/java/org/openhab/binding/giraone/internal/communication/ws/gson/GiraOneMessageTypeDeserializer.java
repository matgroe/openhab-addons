package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.communication.ws.GiraOneMessageType;

import com.google.gson.*;

class GiraOneMessageTypeDeserializer extends GiraOneMessageJsonTypeAdapter
        implements JsonDeserializer<GiraOneMessageType> {
    @Override
    public GiraOneMessageType deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (isResponse(jsonElement)) {
            return hasError(getResponse(jsonElement)) ? GiraOneMessageType.Error : GiraOneMessageType.Response;
        } else if (isEvent(jsonElement)) {
            return hasError(getEvent(jsonElement)) ? GiraOneMessageType.Error : GiraOneMessageType.Event;
        }
        return GiraOneMessageType.Invalid;
    }
}
