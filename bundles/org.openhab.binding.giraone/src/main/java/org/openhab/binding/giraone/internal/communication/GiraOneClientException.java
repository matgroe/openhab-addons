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

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generic Exception with Gira One Domain.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GiraOneClientException extends RuntimeException {
    public final static String CONNECT_CONFIGURATION = "@text/giraone.client.initiate-websocket.configuration";
    public final static String CONNECT_REFUSED = "@text/giraone.client.initiate-websocket.connect-refused";

    @Serial
    private static final long serialVersionUID = 1L;

    public GiraOneClientException(String message) {
        super(message);
    }

    public GiraOneClientException(String message, Throwable t) {
        super(message, t);
    }
}
