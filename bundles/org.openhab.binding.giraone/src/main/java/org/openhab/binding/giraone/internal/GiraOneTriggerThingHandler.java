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

import io.reactivex.rxjava3.annotations.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The {@link GiraOneTriggerThingHandler} is responsible for handling special
 * things concerning the KNXButtons and Trigger channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneTriggerThingHandler extends GiraOneDefaultThingHandler {
    private final ChannelUID channelUID;
    private @Nullable ScheduledFuture<?> scheduledRunnable = null;

    protected enum TriggerState {
        RELEASED,
        PRESSED,
        HOLD
    }

    private final Logger logger = LoggerFactory.getLogger(GiraOneTriggerThingHandler.class);

    public GiraOneTriggerThingHandler(Thing thing) {
        super(thing);
        this.channelUID = new ChannelUID(this.getThing().getUID(), "trigger#state");
    }

    /**
     * 
     */
    @Override
    public void initialize() {
        super.initialize();
        this.updateState(TriggerState.RELEASED);
    }

    /**
     * Handler function for receiving {@link GiraOneChannelValue} which contains
     * the value for an item channel.
     *
     * @param channelValue The value to apply on a Openhab Thing
     */
    protected void onGiraOneChannelValue(GiraOneChannelValue channelValue) {
        logger.debug("onGiraOneChannelValue :: {}", channelValue);
        if (Integer.parseInt(channelValue.getGiraOneValue().getValue()) == 1) {
            this.updateState(TriggerState.PRESSED);
        } else {
            if (this.scheduledRunnable != null) {
                this.scheduledRunnable.cancel(true);
            }
            this.updateState(TriggerState.RELEASED);
        }
    }

    private void scheduleRunnable(Runnable runnable, long delay, @NonNull TimeUnit unit) {
        if (this.scheduledRunnable != null) {
            this.scheduledRunnable.cancel(true);
        }
        this.scheduledRunnable = this.scheduler.schedule(runnable, delay, unit);
    }

    protected void updateState(TriggerState triggerState) {
        logger.debug("Update {} to {}", channelUID.getAsString(), triggerState);
        if (triggerState == TriggerState.PRESSED) {
            this.scheduleRunnable(this::setStateHOLD, 1200, TimeUnit.MILLISECONDS);
        } else if (triggerState == TriggerState.HOLD) {
            this.scheduleRunnable(this::setStateReleased, 90, TimeUnit.SECONDS);
        }
        super.updateState(channelUID, StringType.valueOf(triggerState.toString()));
    }

    private void setStateHOLD() {
        this.updateState(TriggerState.HOLD);
    }

    private void setStateReleased() {
        this.updateState(TriggerState.RELEASED);
    }
}
