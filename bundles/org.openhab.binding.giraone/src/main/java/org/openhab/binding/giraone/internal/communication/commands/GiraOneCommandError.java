package org.openhab.binding.giraone.internal.communication.commands;

import com.google.gson.annotations.SerializedName;

public class GiraOneCommandError {

    @SerializedName("text")
    private String error;

    @SerializedName("hint")
    private String hint;

    @SerializedName("code")
    private int code;

    public String getHint() {
        return hint;
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("GiraOneCommandError: %d -- %s (%s)", code, error, hint);
    }
}
