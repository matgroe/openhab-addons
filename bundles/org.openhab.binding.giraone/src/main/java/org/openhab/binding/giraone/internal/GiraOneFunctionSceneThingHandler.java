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

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.CHANNEL_EXECUTE;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;

import io.reactivex.rxjava3.core.Observable;

/**
 * The {@link GiraOneFunctionSceneThingHandler} is responsible for handling commands, which are
 * sent to one of the function/scene channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneFunctionSceneThingHandler extends GiraOneDefaultThingHandler {
    public GiraOneFunctionSceneThingHandler(Thing thing) {
        super(thing);
    }

    private void resetFunctionSceneState(Long aLong) {
        updateState(CHANNEL_EXECUTE, DecimalType.valueOf("0"));
    }

    @Override
    protected void handleDecimalTypeCommand(GiraOneDataPoint datapoint, DecimalType command) {
        super.handleDecimalTypeCommand(datapoint, command);
        Observable.timer(10, TimeUnit.SECONDS).subscribe(this::resetFunctionSceneState);
    }
}
