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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneChannelValue;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Disposable disposableTimer = Disposable.empty();

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
            this.updateState(TriggerState.RELEASED);
        }
    }

    private Observable<Long> createObservableTimer(long delay, @NonNull TimeUnit unit) {
        disposableTimer.dispose();
        return Observable.timer(delay, unit).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).take(1);
    }

    protected void updateState(TriggerState triggerState) {
        logger.debug("Update {} to {}", channelUID.getAsString(), triggerState);
        if (triggerState == TriggerState.PRESSED) {
            disposableTimer = createObservableTimer(1200, TimeUnit.MILLISECONDS)
                    .subscribe(x -> updateState(TriggerState.HOLD));
        } else if (triggerState == TriggerState.HOLD) {
            disposableTimer = createObservableTimer(90, TimeUnit.SECONDS)
                    .subscribe(x -> updateState(TriggerState.RELEASED));
        }
        super.updateState(channelUID, StringType.valueOf(triggerState.toString()));
    }
}
