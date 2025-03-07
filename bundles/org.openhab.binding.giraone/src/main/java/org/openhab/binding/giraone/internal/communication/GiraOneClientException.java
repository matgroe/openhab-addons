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
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generic Exception with Gira One Domain.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({})
public class GiraOneClientException extends RuntimeException {
    public final static String UNKNOWN_ERROR = "@text/giraone.client.unkown-error";
    public final static String UNEXPECTED_CONNECTION_STATE = "@text/giraone.client.unexpected-connection-state";
    public final static String CONNECT_CONFIGURATION = "@text/giraone.client.websocket.configuration";
    public final static String CONNECT_REFUSED = "@text/giraone.client.websocket.connect-refused";
    public final static String WEBSOCKET_COMMUNICATION = "@text/giraone.client.websocket.communication";
    public final static String MESSAGE_TOO_LARGE = "@text/giraone.client.websocket.message-too-large";
    public final static String DISCONNECT_FAILED = "@text/giraone.client.websocket.disconnect";

    @Serial
    private static final long serialVersionUID = 1L;
    private final String[] placeholders;

    public GiraOneClientException(String message) {
        this(message, (Throwable) null);
    }

    public GiraOneClientException(String message, Throwable t) {
        super(message, t);
        this.placeholders = new String[0];
    }

    public GiraOneClientException(String message, String... placeholders) {
        super(message);
        this.placeholders = placeholders;
    }

    private String formatMessagePlaceholder(String message) {
        return String.format(" [\"%s\"]", message);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(Objects.requireNonNullElse(super.getMessage(), UNKNOWN_ERROR));
        if (getCause() != null) {
            sb.append(formatMessagePlaceholder(getCause().getMessage()));
        }
        Arrays.stream(this.placeholders).map(this::formatMessagePlaceholder).forEach(sb::append);
        return sb.toString();
    }
}
