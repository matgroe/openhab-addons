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

import java.util.Arrays;

/**
 * Websocket Close Codes as defined in
 * https://www.iana.org/assignments/websocket/websocket.xhtml#close-code-number
 *
 * @author Matthias Gröger - Initial contribution
 */
public enum GiraOneWebsocketCloseCode {
    NORMAL_CLOSURE(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    CANNOT_ACCEPT(1003),
    RESERVED(1004),
    NO_STATUS_CODE(1005),
    CLOSED_ABNORMALLY(1006),
    NOT_CONSISTENT(1007),
    VIOLATED_POLICY(1008),
    TOO_BIG(1009),
    NO_EXTENSION(1010),
    UNEXPECTED_CONDITION(1011),
    SERVICE_RESTART(1012),
    TRY_AGAIN_LATER(1013),
    TLS_HANDSHAKE_FAILURE(1015);

    private final int code;

    private GiraOneWebsocketCloseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static GiraOneWebsocketCloseCode fromCode(int value) throws IllegalArgumentException {
        return Arrays.stream(GiraOneWebsocketCloseCode.values()).filter(f -> value == f.getCode()).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
