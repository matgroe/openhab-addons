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

import org.eclipse.jdt.annotation.NonNullByDefault;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_STATUS;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link GiraOneHeatingUnderfloorThingHandler} is responsible for handling special
 * things concerning the heating/cooling channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneHeatingUnderfloorThingHandler extends GiraOneDefaultThingHandler {
    public GiraOneHeatingUnderfloorThingHandler(Thing thing) {
        super(thing);
    }

    private String mapDataPointChannelName(String channelName) {
        // we need to deal with the heater mode in a special way.
        // Gira One uses the channel "status" for providing the
        // current heater mode, but uses the channel "mode" for
        // changing the concerning value. At this point, we're mapping
        // the received value from "status" to the internal channel "mode"

        if (CHANNEL_STATUS.equals(channelName)) {
            return CHANNEL_MODE;
        }
        // otherwise, process as usual
        return channelName;
    }

    protected void updateState(String channelID, State state) {
        if (CHANNEL_MODE.equalsIgnoreCase(mapDataPointChannelName(channelID)) && state.toString().isEmpty()) {
            return;
        }
        super.updateState(mapDataPointChannelName(channelID), state);
    }

    @Override
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        if (CHANNEL_STATUS.equals(dataPoint.getName())) {
            return CHANNEL_MODE;
        }
        // otherwise, process as usual
        return super.buildThingChannelId(dataPoint);
    }
}
