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
package org.openhab.binding.bluetooth.grundfosalpha.internal;

import static org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.grundfosalpha.internal.handler.GrundfosAlpha3Handler;
import org.openhab.binding.bluetooth.grundfosalpha.internal.handler.GrundfosAlphaReaderHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link GrundfosAlphaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bluetooth.grundfosalpha", service = ThingHandlerFactory.class)
public class GrundfosAlphaHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MI401.equals(thingTypeUID)) {
            return new GrundfosAlphaReaderHandler(thing);
        } else if (THING_TYPE_ALPHA3.equals(thingTypeUID)) {
            return new GrundfosAlpha3Handler(thing);
        }

        return null;
    }
}
