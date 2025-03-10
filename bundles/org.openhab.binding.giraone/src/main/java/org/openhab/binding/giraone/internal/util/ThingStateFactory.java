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
package org.openhab.binding.giraone.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.GiraOneBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Test class for {@link ThingStateFactory}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault()
public abstract class ThingStateFactory {

    public static State from(final String channelId, String value) {
        switch (channelId) {
            case GiraOneBindingConstants.CHANNEL_ON_OFF:
                return OnOffType.from(value);

            // case GiraOneBindingConstants.CHANNEL_SHIFT:
            // case GiraOneBindingConstants.CHANNEL_BRIGHTNESS:
            // case GiraOneBindingConstants.CHANNEL_STEP_UP_DOWN:
            // case GiraOneBindingConstants.CHANNEL_UP_DOWN:
            // case GiraOneBindingConstants.CHANNEL_MOVEMENT:
            // case GiraOneBindingConstants.CHANNEL_POSITION:
            // case GiraOneBindingConstants.CHANNEL_SLAT_POSITION:
            // case GiraOneBindingConstants.CHANNEL_CURRENT:
            // case GiraOneBindingConstants.CHANNEL_SET_POINT:
            // case GiraOneBindingConstants.CHANNEL_MODE:
            // case GiraOneBindingConstants.CHANNEL_STATUS:
            // case GiraOneBindingConstants.CHANNEL_PRESENCE:
            // case GiraOneBindingConstants.CHANNEL_HEATING:
            // case GiraOneBindingConstants.CHANNEL_COOLING:
            // case GiraOneBindingConstants.CHANNEL_HEAT_COOL:
            case GiraOneBindingConstants.CHANNEL_FLOAT:
                return DecimalType.valueOf(value);

            // case GiraOneBindingConstants.CHANNEL_EXECUTE:
            // case GiraOneBindingConstants.CHANNEL_TEACH:
            // case GiraOneBindingConstants.CHANNEL_MOVEMENT:
            default:
                return StringType.valueOf(value);
        }
    }
}
