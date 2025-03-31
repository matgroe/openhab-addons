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

package org.openhab.binding.giraone.internal.communication.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for generating reliable incrementing sequence ids.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault()
public class ServerCommandSequence {

    private static int counter = 1;

    public static synchronized void reset() {
        counter = 1;
    }

    public static synchronized int generate() {
        return ++counter;
    }
}
