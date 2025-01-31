package org.openhab.binding.giraone.internal.communication.ws.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class GiraOneMessageJsonTypeAdapter {
    protected static final String PROPERTY_RESPONSE = "response";
    protected static final String PROPERTY_EVENT = "event";
    protected static final String PROPERTY_ERROR = "error";

    boolean hasError(JsonObject jsonObject) {
        if (jsonObject != null && jsonObject.has(PROPERTY_ERROR)) {
            return !"OK".equalsIgnoreCase(jsonObject.getAsJsonObject(PROPERTY_ERROR).get("text").getAsString());
        }
        return false;
    }

    boolean isResponse(JsonElement jsonElement) {
        return jsonElement != null && isResponse(jsonElement.getAsJsonObject());
    }

    boolean isResponse(JsonObject jsonObject) {
        return jsonObject != null && jsonObject.has(PROPERTY_RESPONSE);
    }

    JsonObject getResponse(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(PROPERTY_RESPONSE).getAsJsonObject();
    }

    boolean isEvent(JsonElement jsonElement) {
        return jsonElement != null && isEvent(jsonElement.getAsJsonObject());
    }

    boolean isEvent(JsonObject jsonObject) {
        return jsonObject != null && jsonObject.has(PROPERTY_EVENT);
    }

    JsonObject getEvent(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(PROPERTY_EVENT).getAsJsonObject();
    }
}
