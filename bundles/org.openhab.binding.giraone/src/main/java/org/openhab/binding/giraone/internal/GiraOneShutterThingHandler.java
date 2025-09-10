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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_MOTION_STATE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_SHUTTER_STATE;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_UP_DOWN;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneURN;
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
    static final int THRESHOLD_CLOSED = 95;

    protected enum Direction {
        UNDEFINED,
        UP,
        DOWN
    }

    protected enum MotionState {
        HALTED,
        MOVING,
        MOVING_UP,
        MOVING_DOWN,
    };

    protected enum ShutterState {
        OPEN,
        CLOSED
    };

    private final Logger logger = LoggerFactory.getLogger(GiraOneShutterThingHandler.class);
    private MotionState motionState = MotionState.HALTED;
    private Direction direction = Direction.UNDEFINED;

    public GiraOneShutterThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateChannelShutterMotionState(MotionState.HALTED);
    }

    private void lookupPosition(GiraOneValue value) {
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                GiraOneURN valueUrn = GiraOneURN.of(value.getDatapointUrn());
                GiraOneURN positionUrn = GiraOneURN.of(valueUrn.getParent(), "Position");
                getGiraOneBridge().lookupGiraOneDatapointValue(new GiraOneDataPoint(positionUrn));
            }
        }, 500, TimeUnit.MILLISECONDS);
    }

    protected void onChannelMovement(GiraOneValue value) {
        if (value.getValueAsInt() == 1) {
            lookupPosition(value);
            updateChannelShutterMotionState(MotionState.MOVING);
        } else {
            updateChannelShutterMotionState(MotionState.HALTED);
        }
    }

    protected void onChannelPosition(GiraOneValue value) {
        logger.trace("onChannelPosition :: {}", value);
        if (value instanceof GiraOneValueChange change) {
            if (change.isChanged()) {
                this.direction = change.isValueIncreasing() ? Direction.DOWN : Direction.UP;
                logger.debug("onChannelPosition :: position changed from {} to {} - direction is {}",
                        change.getPreviousValueAsFloat(), change.getValueAsFloat(), this.direction);
                updateChannelShutterMotionState(MotionState.MOVING);
            } else {
                this.direction = Direction.UNDEFINED;
                updateChannelShutterMotionState(MotionState.HALTED);
            }
        }
        detectShutterState(value);
        super.onGiraOneValue(value);
    }

    protected void onChannelUpDown(String channelId, GiraOneValue value) {
        logger.trace("onChannelUpDown :: {}", value);
        if (value instanceof GiraOneValueChange change) {

            this.direction = change.getValueAsInt() == 0 ? Direction.UP : Direction.DOWN;
            logger.debug("onChannelUpDown :: value {} changed from {} to {} - direction is {}", value.getDatapointUrn(),
                    change.getPreviousValue(), change.getValue(), this.direction);
        }
        updateState(channelId, StringType.valueOf(this.direction.toString()));
        updateChannelShutterMotionState(MotionState.MOVING);
    }

    protected void detectShutterState(GiraOneValue value) {
        logger.trace("detectShutterState :: {}", value);
        if (value.getValueAsInt() <= THRESHOLD_CLOSED) {
            updateState(CHANNEL_SHUTTER_STATE, StringType.valueOf(ShutterState.OPEN.toString()));
        } else {
            updateState(CHANNEL_SHUTTER_STATE, StringType.valueOf(ShutterState.CLOSED.toString()));
        }
    }

    private void updateChannelShutterMotionState(MotionState motionState) {
        if (motionState == MotionState.MOVING) {
            switch (this.direction) {
                case UP -> this.motionState = MotionState.MOVING_UP;
                case DOWN -> this.motionState = MotionState.MOVING_DOWN;
                default -> this.motionState = MotionState.MOVING;
            }
        } else {
            this.motionState = MotionState.HALTED;
            updateState(CHANNEL_UP_DOWN, StringType.valueOf(Direction.UNDEFINED.toString()));
            updateState(CHANNEL_STEP_UP_DOWN, StringType.valueOf(Direction.UNDEFINED.toString()));
        }
        updateState(CHANNEL_MOTION_STATE, StringType.valueOf(this.motionState.toString()));
    }

    @Override
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        String thingChannelId = super.buildThingChannelId(dataPoint);
        if ("movement".equalsIgnoreCase(thingChannelId)) {
            return CHANNEL_MOTION_STATE;
        }
        return thingChannelId;
    }

    @Override
    protected void onGiraOneValue(GiraOneValue value) {
        logger.debug("onGiraOneValue :: {}", value);
        String channelId = buildThingChannelId(value);
        switch (channelId) {
            case GiraOneBindingConstants.CHANNEL_MOTION_STATE -> onChannelMovement(value);
            case GiraOneBindingConstants.CHANNEL_POSITION -> onChannelPosition(value);
            case GiraOneBindingConstants.CHANNEL_UP_DOWN, GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN ->
                onChannelUpDown(channelId, value);
            default -> super.onGiraOneValue(value);
        }
    }

    @Override
    protected void handleStopMoveTypeCommand(GiraOneDataPoint datapoint, StopMoveType command) {
        if (this.motionState != MotionState.HALTED) {
            GiraOneURN stepUpDownUrn = GiraOneURN.of(datapoint.getDeviceUrn(), "Step-Up-Down");
            Objects.requireNonNull(this.getGiraOneBridge())
                    .setGiraOneDataPointValue(new GiraOneDataPoint(stepUpDownUrn), Integer.toString(0));
        }
    }
}
