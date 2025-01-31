package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.model.GiraOneItemMainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

public class GiraOneItemMainTypeDeserializer implements JsonDeserializer<GiraOneItemMainType> {
    private final Logger logger = LoggerFactory.getLogger(GiraOneItemMainTypeDeserializer.class);

    @Override
    public GiraOneItemMainType deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return GiraOneItemMainType.valueOf(jsonElement.getAsString());
        } catch (IllegalArgumentException exp) {
            logger.error(exp.getMessage(), exp);
            return GiraOneItemMainType.Unknown;
        }
    }
}
