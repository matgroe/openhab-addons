package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openhab.binding.giraone.internal.model.GiraOneProject;
import org.openhab.binding.giraone.internal.model.GiraOneProjectChannel;
import org.openhab.binding.giraone.internal.model.GiraOneProjectItem;

import com.google.gson.*;

public class GiraOneProjectDeserializer implements JsonDeserializer<GiraOneProject> {
    private final static String PROPERTY_FUNCTION_TYPE = "functionType";

    private final static String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    private final static String PROPERTY_CHANNEL_VIEW_URN = "channelViewUrn";
    private final static String PROPERTY_CHANNEL_TYPE = "channelType";
    private final static String PROPERTY_CHANNEL_TYPE_ID = "channelTypeId";

    private final static String CONTENT_ROOT_MAIN_TYPE = "Root";
    private final static String CONTENT_ROOT_SUB_TYPE = CONTENT_ROOT_MAIN_TYPE;
    private final static String PROPERTY_CONTENT_MAIN_TYPE = "mainType";
    private final static String PROPERTY_CONTENT_SUB_TYPE = "subType";

    @Override
    public GiraOneProject deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonArray()) {
            throw new JsonParseException("JsonArray expected here.");
        }
        JsonArray content = jsonElement.getAsJsonArray();
        Optional<JsonElement> jsonRoot = content.asList().stream().filter(this::isProjectContentRootObject).findFirst();
        if (jsonRoot.isPresent()) {
            GiraOneProjectItem root = jsonDeserializationContext.deserialize(jsonRoot.get(), GiraOneProjectItem.class);

            List<GiraOneProjectChannel> channels = content.asList().stream().filter(this::isProjectChannelObject)
                    .map(f -> makeChannel(f, jsonDeserializationContext)).collect(Collectors.toUnmodifiableList());

            return new GiraOneProject(root, channels);
        }
        throw new JsonParseException("Cannot parse received JsonArray.");
    }

    private boolean isProjectContentRootObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has(PROPERTY_CONTENT_MAIN_TYPE) && jsonObject.has(PROPERTY_CONTENT_SUB_TYPE)) {
                return CONTENT_ROOT_MAIN_TYPE.equals(jsonObject.get(PROPERTY_CONTENT_MAIN_TYPE).getAsString())
                        && CONTENT_ROOT_SUB_TYPE.equals(jsonObject.get(PROPERTY_CONTENT_SUB_TYPE).getAsString());
            }
        }
        return false;
    }

    private boolean isProjectChannelObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has(PROPERTY_CHANNEL_VIEW_ID) && jsonObject.has(PROPERTY_CHANNEL_VIEW_URN)
                    && jsonObject.has(PROPERTY_CHANNEL_TYPE) && jsonObject.has(PROPERTY_CHANNEL_TYPE_ID);
        }
        return false;
    }

    private GiraOneProjectChannel makeChannel(JsonElement jsonElement,
            JsonDeserializationContext jsonDeserializationContext) {
        return jsonDeserializationContext.deserialize(jsonElement, GiraOneProjectChannel.class);
    }
}
