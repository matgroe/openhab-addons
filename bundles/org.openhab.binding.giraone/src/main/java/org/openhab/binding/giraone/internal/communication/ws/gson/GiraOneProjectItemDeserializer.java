package org.openhab.binding.giraone.internal.communication.ws.gson;

import java.lang.reflect.Type;
import java.util.Arrays;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.openhab.binding.giraone.internal.model.GiraOneChannelViewRef;
import org.openhab.binding.giraone.internal.model.GiraOneItemMainType;
import org.openhab.binding.giraone.internal.model.GiraOneItemSubType;
import org.openhab.binding.giraone.internal.model.GiraOneProjectItem;

import com.google.gson.*;

public class GiraOneProjectItemDeserializer implements JsonDeserializer<GiraOneProjectItem> {
    private final static String PROPERTY_CHANNEL_VIEW_ID = "channelViewID";
    private final static String PROPERTY_MAIN_TYPE = "mainType";
    private final static String PROPERTY_SUB_TYPE = "subType";
    private final static String PROPERTY_NAME = "name";
    private final static String PROPERTY_URN = "urn";
    private final static String PROPERTY_CONTENT = "content";
    private final static String PROPERTY_CHILDREN = "children";
    private final static String PROPERTY_ITEM_REFERENCES = "itemReferences";

    @Override
    public GiraOneProjectItem deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonObject()) {
            throw new JsonParseException("JsonObject expected here.");
        }
        GiraOneProjectItem item = new GiraOneProjectItem();
        ;
        try {
            JsonObject json = jsonElement.getAsJsonObject();

            if (isProjectItem(json)) {
                FieldUtils.writeField(item, PROPERTY_MAIN_TYPE,
                        jsonDeserializationContext.deserialize(json.get(PROPERTY_MAIN_TYPE), GiraOneItemMainType.class),
                        true);
                FieldUtils.writeField(item, PROPERTY_SUB_TYPE,
                        jsonDeserializationContext.deserialize(json.get(PROPERTY_SUB_TYPE), GiraOneItemSubType.class),
                        true);
                FieldUtils.writeField(item, PROPERTY_NAME, json.get(PROPERTY_NAME).getAsString(), true);
                FieldUtils.writeField(item, PROPERTY_URN, json.get(PROPERTY_URN).getAsString(), true);

                if (hasProjectItemContentArray(json)) {
                    JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
                    GiraOneProjectItem[] items = jsonDeserializationContext.deserialize(contentArray,
                            GiraOneProjectItem[].class);
                    FieldUtils.writeField(item, PROPERTY_CHILDREN, Arrays.stream(items).toList(), true);
                } else if (hasChannelRefContentArray(json)) {
                    JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
                    GiraOneChannelViewRef[] items = jsonDeserializationContext.deserialize(contentArray,
                            GiraOneChannelViewRef[].class);
                    FieldUtils.writeField(item, PROPERTY_ITEM_REFERENCES, Arrays.stream(items).toList(), true);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    private boolean isProjectItem(JsonObject json) {
        return json.has(PROPERTY_MAIN_TYPE) && json.has(PROPERTY_SUB_TYPE) && json.has(PROPERTY_NAME)
                && json.has(PROPERTY_URN);
    }

    private boolean hasChannelRefContentArray(JsonObject json) {
        if (json.has(PROPERTY_CONTENT) && json.get(PROPERTY_CONTENT).isJsonArray()) {
            JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
            if (!contentArray.isEmpty()) {
                return isChannelRef(contentArray.get(0).getAsJsonObject());
            }
        }
        return false;
    }

    private boolean isChannelRef(JsonObject json) {
        return json.has(PROPERTY_CHANNEL_VIEW_ID);
    }

    private boolean hasProjectItemContentArray(JsonObject json) {
        if (json.has(PROPERTY_CONTENT) && json.get(PROPERTY_CONTENT).isJsonArray()) {
            JsonArray contentArray = json.get(PROPERTY_CONTENT).getAsJsonArray();
            if (!contentArray.isEmpty()) {
                return isProjectItem(contentArray.get(0).getAsJsonObject());
            }
        }
        return false;
    }
}
