package org.openhab.binding.giraone.internal.model;

import com.google.gson.annotations.SerializedName;

public class GiraOneDataPoint {

    @SerializedName(value = "dataPoint")
    private String dataPoint;
    private int id;
    private String urn;

    public String getDataPoint() {
        return dataPoint;
    }

    public int getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }
}
