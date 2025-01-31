package org.openhab.binding.giraone.internal.model;

public class GiraOneChannelViewRef implements GiraOneItemReference {

    private int channelViewID;

    @Override
    public int getReferenceId() {
        return channelViewID;
    }
}
