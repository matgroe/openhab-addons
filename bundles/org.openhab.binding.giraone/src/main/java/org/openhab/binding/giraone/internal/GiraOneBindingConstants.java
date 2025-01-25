/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GiraOneBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneBindingConstants {

    // server config properties
    public static final String HOST = "hostname";
    public static final String USER_NAME = "username";

    private static final String BINDING_ID = "giraone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_G1_SERVER = new ThingTypeUID(BINDING_ID, "server");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
}
