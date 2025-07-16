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

/**
 * Configuration settings for connecting Gira One Server
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneClientConfiguration {
    private static final String EMPTY = "";

    public String hostname = EMPTY;
    public String username = EMPTY;
    public String password = EMPTY;

    public int defaultTimeoutSeconds = 10;
    public int maxTextMessageSize = 100; // 100kB
    public int tryReconnectAfterSeconds = 30;
    public int buttonReleaseTimeout = 1200;
    public boolean discoverDevices = true;
    public boolean discoverButtons = true;
    public boolean overrideWithProjectSettings = false;
}
