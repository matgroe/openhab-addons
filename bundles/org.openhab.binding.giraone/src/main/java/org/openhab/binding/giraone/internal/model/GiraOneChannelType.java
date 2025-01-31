package org.openhab.binding.giraone.internal.model;

import java.util.Arrays;

public enum GiraOneChannelType {
    Dimmer("de.gira.schema.channels.KNX.Dimmer"),
    BlindWithPos("de.gira.schema.channels.BlindWithPos"),
    HeatingCoolingSwitchable("de.gira.schema.channels.KNX.HeatingCoolingSwitchable"),
    Float("de.gira.schema.channels.Float"),
    FunctionScene("de.gira.schema.channels.FunctionScene"),
    Switch("de.gira.schema.channels.Switch"),
    Unknown("Unknown");

    private final String name;

    private GiraOneChannelType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneChannelType fromName(String value) {
        return Arrays.stream(GiraOneChannelType.values()).filter(f -> value.equals(f.name)).findFirst().orElse(Unknown);
    }
}
