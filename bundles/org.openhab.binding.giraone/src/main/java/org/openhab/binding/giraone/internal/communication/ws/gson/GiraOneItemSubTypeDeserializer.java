package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;

import org.openhab.binding.giraone.internal.model.GiraOneItemSubType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

public class GiraOneItemSubTypeDeserializer implements JsonDeserializer<GiraOneItemSubType> {
    private final Logger logger = LoggerFactory.getLogger(GiraOneItemSubTypeDeserializer.class);

    @Override
    public GiraOneItemSubType deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return GiraOneItemSubType.valueOf(jsonElement.getAsString());
        } catch (IllegalArgumentException exp) {
            logger.error(exp.getMessage(), exp);
            return GiraOneItemSubType.Unknown;
        }
    }
}
