package org.openhab.binding.giraone.internal.util;

import org.openhab.binding.giraone.internal.GiraOneBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

public abstract class ThingStateFactory {

    public static State from(final String channelId, String value) {
        switch (channelId) {
            case GiraOneBindingConstants.CHANNEL_ON_OFF:
            case GiraOneBindingConstants.CHANNEL_MOVEMENT:
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

            default:
                return StringType.valueOf(value);

        }
    }
}
