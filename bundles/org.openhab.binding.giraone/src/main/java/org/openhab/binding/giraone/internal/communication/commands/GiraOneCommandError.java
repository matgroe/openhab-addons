package org.openhab.binding.giraone.internal.communication.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class GiraOneCommandError {

    @SerializedName("text")
    private String error = "OK";

    @SerializedName("hint")
    private String hint = "";

    @SerializedName("code")
    private int code = 0;

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
