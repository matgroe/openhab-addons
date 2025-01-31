package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.model.GiraOneChannelType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GiraOneChannelTypeDeserializer implements JsonDeserializer<GiraOneChannelType> {

    @Override
    public GiraOneChannelType deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return GiraOneChannelType.fromName(jsonElement.getAsString());
    }
}
