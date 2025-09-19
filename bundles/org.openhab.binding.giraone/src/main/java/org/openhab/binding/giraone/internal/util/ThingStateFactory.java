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
import org.openhab.core.library.types.DateTimeType;
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
        return switch (channelId) {
            case GiraOneBindingConstants.CHANNEL_ON_OFF -> OnOffType.from(value);
            case GiraOneBindingConstants.CHANNEL_SERVER_TIME -> DateTimeType.valueOf(value);
            case GiraOneBindingConstants.CHANNEL_FLOAT -> roundWithTwoDecimals(value);
            case GiraOneBindingConstants.CHANNEL_POSITION, GiraOneBindingConstants.CHANNEL_SLAT_POSITION ->
                roundAsInteger(value);
            default -> StringType.valueOf(value);
        };
    }

    private static DecimalType roundAsInteger(String value) {
        return new DecimalType(Math.round(Float.parseFloat(value)));
    }

    private static DecimalType roundWithTwoDecimals(String value) {
        return new DecimalType((float) (Math.round(Float.parseFloat(value) * 100)) / 100);
    }
}
