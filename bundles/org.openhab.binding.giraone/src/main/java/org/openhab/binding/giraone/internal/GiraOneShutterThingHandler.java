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

    private enum MovingState {
        HALTED,
        MOVING,
        MOVING_UP,
        MOVING_DOWN,
    };

    private final Logger logger = LoggerFactory.getLogger(GiraOneShutterThingHandler.class);
    private MovingState movingState = MovingState.HALTED;

    public GiraOneShutterThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateChannelShutterMovingState(MovingState.HALTED);
    }

    private void detectMovingState(GiraOneValue value) {
        if ("0".equals(value.getValue())) {
            updateChannelShutterMovingState(MovingState.HALTED);
        } else if (this.movingState == MovingState.HALTED) {
            updateChannelShutterMovingState(MovingState.MOVING);
        }
    }

    private void detectMovingDirection(GiraOneValue value) {
        if (value instanceof GiraOneValueChange) {
            updateChannelShutterMovingState(
                    isValueIncreasing((GiraOneValueChange) value) ? MovingState.MOVING_DOWN : MovingState.MOVING_UP);
        }
    }

    private void updateChannelShutterMovingState(MovingState movingState) {
        this.logger.debug("updateChannelShutterMovement :: {}", movingState.toString());
        this.movingState = movingState;
        updateState(CHANNEL_ID_SHUTTER_MOVEMENT, StringType.valueOf(movingState.toString()));
    }

    @Override
    protected void onGiraOneChannelValue(GiraOneChannelValue giraOneChannelValue) {
        logger.debug("onGiraOneChannelValue :: {}", giraOneChannelValue);
        switch (buildThingChannelId(giraOneChannelValue)) {
            case GiraOneBindingConstants.CHANNEL_MOVEMENT -> detectMovingState(giraOneChannelValue.getGiraOneValue());

            case GiraOneBindingConstants.CHANNEL_POSITION, GiraOneBindingConstants.CHANNEL_UP_DOWN,
                    GiraOneBindingConstants.CHANNEL_SLAT_POSITION, GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN ->
                detectMovingDirection(giraOneChannelValue.getGiraOneValue());
        }
        super.onGiraOneChannelValue(giraOneChannelValue);
    }

    @Override
    protected void handleStopMoveTypeCommand(GiraOneDataPoint datapoint, StopMoveType command) {
        logger.trace("handleStopMoveType :: datapoint={}, command={}", datapoint.getId(), command.name());
        Optional<GiraOneDataPoint> stepUpDown = super.findGiraOneDataPointWithinChannelView(
                GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN);
        if (stepUpDown.isPresent() && this.movingState != MovingState.HALTED) {
            // we need to set value 0 on channel 'step-up-down' to stop moving curtain
            Objects.requireNonNull(this.getGiraOneBridge()).setGiraOneDataPointValue(stepUpDown.get(),
                    Integer.toString(0));
        }
    }
}
