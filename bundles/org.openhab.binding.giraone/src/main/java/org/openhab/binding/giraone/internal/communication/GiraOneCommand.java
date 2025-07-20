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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines a command to be sent out via websocket or
 * webservice api to Gira One Server.
 *
 * @author Matthias Groeger - Initial contribution
 */
public class GiraOneCommand {
    private static final String MISSING_ANNOTATION = "";

    private GiraOneServerCommand getAnnotation() {
        if (getClass().isAnnotationPresent(GiraOneServerCommand.class)) {
            return getClass().getAnnotation(GiraOneServerCommand.class);
        }
        throw new IllegalArgumentException(MISSING_ANNOTATION);
    }

    public String getCommand() {
        return getAnnotation().name();
    }

    public String getResponsePropertyName() {
        return getAnnotation().responsePayload();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof GiraOneCommand) {
            return getCommand().equals(((GiraOneCommand) o).getCommand());
        }
        return false;
    }
}
