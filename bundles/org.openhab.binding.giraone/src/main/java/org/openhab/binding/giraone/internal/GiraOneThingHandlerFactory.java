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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.BINDING_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.BRIDGE_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.HEATING_COOLING_TYPE_ID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.SUPPORTED_FUNCTION_SCENE_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.SUPPORTED_SHUTTER_THING_TYPE_UID;
import static org.openhab.binding.giraone.internal.GiraOneBindingConstants.TRIGGER_BUTTON_ID;

/**
 * The {@link GiraOneThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.giraone", service = ThingHandlerFactory.class)
public class GiraOneThingHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.getBindingId().equals(BINDING_ID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thing instanceof Bridge && BRIDGE_TYPE_UID.equals(thingTypeUID)) {
            return new GiraOneBridgeHandler((Bridge) thing);
        } else if (SUPPORTED_SHUTTER_THING_TYPE_UID.contains(thingTypeUID)) {
            return new GiraOneShutterThingHandler(thing);
        } else if (SUPPORTED_FUNCTION_SCENE_TYPE_UID.contains(thingTypeUID)) {
            return new GiraOneFunctionSceneThingHandler(thing);
        } else if (HEATING_COOLING_TYPE_ID.equals(thingTypeUID.getId())) {
            return new GiraOneHeatingUnderfloorThingHandler(thing);
        } else if (TRIGGER_BUTTON_ID.equals(thingTypeUID.getId())) {
            return new GiraOneTriggerThingHandler(thing);
        } else {
            return new GiraOneDefaultThingHandler(thing);
        }
    }
}
