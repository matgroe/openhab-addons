package org.openhab.binding.giraone.internal.model;

import java.util.Arrays;

public enum GiraOneChannelTypeId {
    Float("NumericFloatStatus.Temperatur"),
    FunctionScene("FunctionScene.Scene"),
    HeatingCoolingSwitchable("KNX.HeatingCooling.HeatingUnderfloorHeatingWaterBased"),
    Light("KNX.Light.Light"),
    SwitchLamp("Switch.Lamp"),
    SwitchPowerOutlet("Switch.PowerOutlet"),
    CoveringVenetianBlind("Covering.VenetianBlind"),
    CoveringRoofWindow("Covering.RoofWindow"),
    CoveringAwning("Covering.Awning"),
    Unknown("Unknown");

    private final String name;

    private GiraOneChannelTypeId(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneChannelTypeId fromName(String value) {
        return Arrays.stream(GiraOneChannelTypeId.values()).filter(f -> value.equals(f.name)).findFirst()
                .orElse(Unknown);
    }
}
