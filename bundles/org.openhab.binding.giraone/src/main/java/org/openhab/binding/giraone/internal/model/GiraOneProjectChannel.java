package org.openhab.binding.giraone.internal.model;

import java.util.Collection;

import com.google.gson.annotations.SerializedName;

public class GiraOneProjectChannel {

    @SerializedName(value = "channelID")
    private int channelId;
    private String channelUrn;

    @SerializedName(value = "channelViewID")
    private int channelViewId;
    private String channelViewUrn;
    private GiraOneFunctionType functionType;
    private GiraOneChannelType channelType;
    private GiraOneChannelTypeId channelTypeId;
    private String name;

    @SerializedName(value = "iconID")
    private int iconId;

    private Collection<GiraOneDataPoint> dataPoints;

    public int getChannelId() {
        return channelId;
    }

    public String getChannelUrn() {
        return channelUrn;
    }

    public int getChannelViewId() {
        return channelViewId;
    }

    public String getChannelViewUrn() {
        return channelViewUrn;
    }

    public GiraOneFunctionType getFunctionType() {
        return functionType;
    }

    public GiraOneChannelType getChannelType() {
        return channelType;
    }

    public GiraOneChannelTypeId getChannelTypeId() {
        return channelTypeId;
    }

    public String getName() {
        return name;
    }

    public int getIconId() {
        return iconId;
    }

    public Collection<GiraOneDataPoint> getDataPoints() {
        return dataPoints;
    }

    @Override
    public String toString() {
        return String.format("%s(%s):{channelId=%d, channelUrn=%s, channelViewId=%d, channelViewUrn=%s}",
                getClass().getSimpleName(), name, channelId, channelUrn, channelViewId, channelViewUrn);
    }
}
