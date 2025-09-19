/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.giraone.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GiraOneBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneBindingConstants {

    // Thing Property Names
    public static final String PROPERTY_CHANNEL_URN = "channelUrn";
    public static final String PROPERTY_FUNCTION_TYPE = "functionType";
    public static final String PROPERTY_CHANNEL_TYPE = "channelType";
    public static final String PROPERTY_CHANNEL_TYPE_ID = "channelTypeId";
    public static final String PROPERTY_CHANNEL_NAME = "channelName";
    public static final String PROPERTY_CHANNEL_DATAPOINTS = "datapoints";

    // List of all ThingTypeIDs
    public static final String BINDING_ID = "giraone";
    public static final String BRIDGE_TYPE_ID = "server";
    public static final String GENERIC_TYPE_ID = "generic";

    public static final String DIMMER_TYPE_ID = "dimmer-light";
    public static final String SCENE_TYPE_ID = "function-scene";
    public static final String HEATING_COOLING_TYPE_ID = "heating-underfloor";
    public static final String SHUTTER_AWNING_TYPE_ID = "shutter-awning";
    public static final String SHUTTER_ROOF_WINDOW_TYPE_ID = "shutter-roof-window";
    public static final String SHUTTER_VENETIAN_BLIND_TYPE_ID = "shutter-venetian-blind";
    public static final String HUMIDITY_TYPE_ID = "status-humidity";
    public static final String TEMPERATURE_TYPE_ID = "status-temperature";
    public static final String SWITCH_LAMP_TYPE_ID = "switch-lamp";
    public static final String SWITCH_POWER_OUTLET_TYPE_ID = "switch-power-outlet";
    public static final String TRIGGER_BUTTON_ID = "trigger-button";

    /**
     * The {@link ThingTypeUID} for the GiraOne Bridge
     */
    public static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final ThingTypeUID GENERIC_TYPE_UID = new ThingTypeUID(BINDING_ID, GENERIC_TYPE_ID);

    public static final Set<ThingTypeUID> SUPPORTED_SHUTTER_THING_TYPE_UID = Set.of(
            new ThingTypeUID(BINDING_ID, SHUTTER_VENETIAN_BLIND_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SHUTTER_AWNING_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SHUTTER_ROOF_WINDOW_TYPE_ID));

    public static final Set<ThingTypeUID> SUPPORTED_GENERIC_TYPE_UID = Set.of(BRIDGE_TYPE_UID, GENERIC_TYPE_UID,
            new ThingTypeUID(BINDING_ID, DIMMER_TYPE_ID), new ThingTypeUID(BINDING_ID, HEATING_COOLING_TYPE_ID),
            new ThingTypeUID(BINDING_ID, HUMIDITY_TYPE_ID), new ThingTypeUID(BINDING_ID, TEMPERATURE_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SWITCH_LAMP_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SWITCH_POWER_OUTLET_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SHUTTER_VENETIAN_BLIND_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SHUTTER_AWNING_TYPE_ID),
            new ThingTypeUID(BINDING_ID, SHUTTER_ROOF_WINDOW_TYPE_ID), new ThingTypeUID(BINDING_ID, TRIGGER_BUTTON_ID));

    public static final Set<ThingTypeUID> SUPPORTED_FUNCTION_SCENE_TYPE_UID = Set
            .of(new ThingTypeUID(BINDING_ID, SCENE_TYPE_ID));

    /**
     * List of all supported {@link ThingTypeUID} within this binding.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UID = Stream
            .concat(Stream.concat(SUPPORTED_SHUTTER_THING_TYPE_UID.stream(), SUPPORTED_GENERIC_TYPE_UID.stream()),
                    SUPPORTED_FUNCTION_SCENE_TYPE_UID.stream())
            .collect(Collectors.toUnmodifiableSet());

    // List of all Channel ids
    public static final String CHANNEL_ON_OFF = "on-off";
    public static final String CHANNEL_SHIFT = "shift";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_STEP_UP_DOWN = "step-up-down";
    public static final String CHANNEL_UP_DOWN = "up-down";
    public static final String CHANNEL_MOTION_STATE = "motion-state";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_SLAT_POSITION = "slat-position";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_SET_POINT = "set-point";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_HEATING = "heating";
    public static final String CHANNEL_COOLING = "cooling";
    public static final String CHANNEL_HEAT_COOL = "heat-cool";
    public static final String CHANNEL_FLOAT = "float";
    public static final String CHANNEL_EXECUTE = "execute";
    public static final String CHANNEL_SHUTTER_STATE = "shutter-state";
    public static final String CHANNEL_SERVER_TIME = "server-time";
    public static final String CHANNEL_CONNECT_TIME = "connect-time";
}
