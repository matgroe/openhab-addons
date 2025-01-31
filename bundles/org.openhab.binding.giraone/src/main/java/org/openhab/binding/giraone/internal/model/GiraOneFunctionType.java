package org.openhab.binding.giraone.internal.model;

import java.util.Arrays;

public enum GiraOneFunctionType {
    Light("de.gira.schema.functions.KNX.Light"),
    Covering("de.gira.schema.functions.Covering"),
    HeatingCooling("de.gira.schema.functions.KNX.HeatingCooling"),
    FloatStatus("de.gira.schema.functions.NumericFloatStatus"),
    FunctionScene("de.gira.schema.functions.FunctionScene"),
    Switch("de.gira.schema.functions.Switch"),
    Unknown("Unknown");

    private final String name;

    private GiraOneFunctionType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GiraOneFunctionType fromName(String value) {
        return Arrays.stream(GiraOneFunctionType.values()).filter(f -> value.equals(f.name)).findFirst()
                .orElse(Unknown);
    }
}
