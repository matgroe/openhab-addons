package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.model.GiraOneFunctionType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GiraOneFunctionTypeDeserializer implements JsonDeserializer<GiraOneFunctionType> {

    @Override
    public GiraOneFunctionType deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return GiraOneFunctionType.fromName(jsonElement.getAsString());
    }
}
