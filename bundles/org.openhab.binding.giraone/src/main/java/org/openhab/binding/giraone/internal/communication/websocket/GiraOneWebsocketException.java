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
package org.openhab.binding.giraone.internal.communication.websocket;

import java.io.Serial;

/**
 * Generic Exception with Gira One Domain.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneWebsocketException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GiraOneWebsocketException(String message) {
        this(message, (Throwable) null);
    }

    public GiraOneWebsocketException(String message, Throwable t) {
        super(message, t);
    }

    public GiraOneWebsocketException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
