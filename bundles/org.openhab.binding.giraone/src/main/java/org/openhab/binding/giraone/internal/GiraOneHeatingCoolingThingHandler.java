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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_STATUS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.core.thing.Thing;

/**
 * The {@link GiraOneHeatingCoolingThingHandler} is responsible for handling commands, which are
 * sent to one of the function/scene channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneHeatingCoolingThingHandler extends GiraOneDefaultThingHandler {
    public GiraOneHeatingCoolingThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        // For Heater Mode: Use channel 'status' to read value, channel 'mode' to set value
        if (CHANNEL_STATUS.equals(dataPoint.getName())) {
            return CHANNEL_MODE;
        }
        return super.buildThingChannelId(dataPoint);
    }
}
