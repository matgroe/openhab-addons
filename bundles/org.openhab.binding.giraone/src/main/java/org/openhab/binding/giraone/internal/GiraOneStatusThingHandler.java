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
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.core.thing.Thing;

/**
 * The {@link GiraOneStatusThingHandler} is responsible for handling commands, which are
 * sent to one of the shutter channels.
 *
 * @author matthias - Initial contribution
 */
@NonNullByDefault
public class GiraOneStatusThingHandler extends GiraOneDefaultThingHandler {

    public GiraOneStatusThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String buildThingChannelId(GiraOneDataPoint dataPoint) {
        return "value";
    }
}
