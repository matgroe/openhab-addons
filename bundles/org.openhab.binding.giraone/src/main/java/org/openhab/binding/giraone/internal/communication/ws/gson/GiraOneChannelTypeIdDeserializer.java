package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.model.GiraOneChannelTypeId;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GiraOneChannelTypeIdDeserializer implements JsonDeserializer<GiraOneChannelTypeId> {
    @Override
    public GiraOneChannelTypeId deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return GiraOneChannelTypeId.fromName(jsonElement.getAsString());
    }
}
