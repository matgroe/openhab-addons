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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.dto.GiraOneChannelDataPoint;
import org.openhab.binding.giraone.internal.dto.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.util.CaseFormatter;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GiraOneShutterThingHandler} is responsible for handling commands, which are
 * sent to one of the shutter channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneShutterThingHandler extends GiraOneDefaultThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GiraOneShutterThingHandler.class);
    private boolean isMoving = false;

    public GiraOneShutterThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void onDataPointState(GiraOneChannelDataPoint giraOneDataPointState) {
        String channelId = CaseFormatter.lowerCaseHyphen(giraOneDataPointState.getName());
        this.isMoving = (GiraOneBindingConstants.CHANNEL_MOVEMENT.equals(channelId)
                && "1".equals(giraOneDataPointState.getValue()));
        super.onDataPointState(giraOneDataPointState);
    }

    @Override
    protected void handleStopMoveType(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.trace("handleStopMoveType :: datapoint={}, command={}", datapoint.getId(), command.name());
        Optional<GiraOneDataPoint> stepUpDown = super.findGiraOneDataPoint(
                GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN);
        if (stepUpDown.isPresent() && this.isMoving) {
            // we need to set value 0 on channel 'step-up-down' to stop moving curtain
            Objects.requireNonNull(this.getGiraOneBridge()).setGiraOneDataPointValue(stepUpDown.get(), 0);
        }
    }
}
