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
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneValue;
import org.openhab.binding.giraone.internal.types.GiraOneValueChange;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
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
    private final static String CHANNEL_ID_SHUTTER_MOVEMENT = "internal-movement-state";
    private final static String VALUE_SHUTTER_MOVEMENT_STOPPED = "STOPPED";

    public enum MovingState {
        STOPPED,
        MOVING,
        MOVING_UP,
        MOVING_DOWN,
    };

    private final Logger logger = LoggerFactory.getLogger(GiraOneShutterThingHandler.class);
    MovingState movingState = MovingState.STOPPED;

    public GiraOneShutterThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateChannelShutterMovement();
    }

    private boolean isValueIncreasing(GiraOneValueChange valueChange) {
        return Integer.parseInt(valueChange.getValue()) > Integer.parseInt(valueChange.getPreviousValue());
    }

    private void detectMovingState(GiraOneValue value) {
        boolean isMoving = "1".equals(value.getValue());
        if (isMoving && this.movingState == MovingState.STOPPED) {
            this.movingState = MovingState.MOVING;
        } else {
            this.movingState = MovingState.STOPPED;
        }
        updateChannelShutterMovement();
    }

    private void detectMovingDirection(GiraOneValue value) {
        if (value instanceof GiraOneValueChange) {
            this.movingState = isValueIncreasing((GiraOneValueChange) value) ? MovingState.MOVING_UP
                    : MovingState.MOVING_DOWN;
        } else {
            this.movingState = MovingState.MOVING;
        }
        updateChannelShutterMovement();
    }

    private void updateChannelShutterMovement() {
        updateState(CHANNEL_ID_SHUTTER_MOVEMENT, StringType.valueOf(this.movingState.toString()));
    }

    @Override
    protected void onDataPointState(GiraOneChannelValue giraOneChannelValue) {
        switch (buildThingChannelId(giraOneChannelValue)) {
            case GiraOneBindingConstants.CHANNEL_MOVEMENT -> detectMovingState(giraOneChannelValue.getGiraOneValue());

            case GiraOneBindingConstants.CHANNEL_POSITION, GiraOneBindingConstants.CHANNEL_UP_DOWN,
                    GiraOneBindingConstants.CHANNEL_SLAT_POSITION, GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN ->
                detectMovingDirection(giraOneChannelValue.getGiraOneValue());
        }
        super.onDataPointState(giraOneChannelValue);
    }

    @Override
    protected void handleStopMoveType(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.trace("handleStopMoveType :: datapoint={}, command={}", datapoint.getId(), command.name());
        Optional<GiraOneDataPoint> stepUpDown = super.findGiraOneDataPointWithinChannelView(
                GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN);
        if (stepUpDown.isPresent() && this.movingState != MovingState.STOPPED) {
            // we need to set value 0 on channel 'step-up-down' to stop moving curtain
            Objects.requireNonNull(this.getGiraOneBridge()).setGiraOneDataPointValue(stepUpDown.get(),
                    Integer.toString(0));
        }
    }
}
