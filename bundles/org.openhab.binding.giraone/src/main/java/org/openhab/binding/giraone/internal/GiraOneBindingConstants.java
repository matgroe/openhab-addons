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

import java.util.Set;

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

    static final String BINDING_ID = "giraone";

    // List of all Thing Type UIDs
    static final String SERVER_TYPE_ID = "server";
    static final String DEVICE_TYPE_ID = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, SERVER_TYPE_ID);
    public static final ThingTypeUID DEVICE_TYPE_UID = new ThingTypeUID(BINDING_ID, DEVICE_TYPE_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_UID, DEVICE_TYPE_UID);

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
}
