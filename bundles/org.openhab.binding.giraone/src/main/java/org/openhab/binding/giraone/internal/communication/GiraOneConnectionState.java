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
package org.openhab.binding.giraone.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The enumeration {@link GiraOneConnectionState} is responsible for describing
 * the current connection state between GiraOneBridge and the physical
 * GiraOne Server within your network.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public enum GiraOneConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Error,
    TemporaryUnavailable
}
