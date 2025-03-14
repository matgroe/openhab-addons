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
 * This enumeration defines the command strings which might be
 * sent to the Gira One Server. The server answers the command
 * within as named by "responsePropertyName" within it's JSON
 * response body.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public enum GiraOneCommand {

    /** First Command to send to server, otherwise no events are getting generated */
    RegisterApplication(""),

    /** Reads all Things and channels to be represented by any UI */
    GetUIConfiguration("config"),

    /** Reads a datapoint value */
    GetValue(""),

    /** sets a datapoint value */
    SetValue(""),

    /** Reads the gira one server device configuration */
    GetDeviceConfig("deviceConfig"),

    /** Read the trigger times from server */
    GetNextTriggerTimes("triggerTimes"),

    GetConfiguration("object"),

    GetGiraOneDevices("devices"),

    GetDiagnosticDeviceList("data");

    private final String responsePropertyName;

    GiraOneCommand(String responsePropertyName) {
        this.responsePropertyName = responsePropertyName;
    }

    public String getResponsePropertyName() {
        return responsePropertyName;
    }
}
