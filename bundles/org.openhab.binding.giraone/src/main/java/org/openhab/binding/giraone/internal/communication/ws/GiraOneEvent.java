package org.openhab.binding.giraone.internal.communication.ws;

import com.google.gson.annotations.SerializedName;

public class GiraOneEvent {
    private String id;
    private String urn;
    @SerializedName(value = "new")
    private String newValue;
    @SerializedName(value = "old")
    private String oldValue;
    private String newInternal;
    private String oldInternal;
    private String state;
    private String source;

    public String getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewInternal() {
        return newInternal;
    }

    public String getOldInternal() {
        return oldInternal;
    }

    public String getState() {
        return state;
    }

    public String getSource() {
        return source;
    }
}
