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
import org.openhab.binding.giraone.internal.util.GenericBuilder;

/**
 * {@link ServerCommand} for reading the DiagnosticDeviceList
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public class GetDiagnosticDeviceList extends ServerCommand {

    public static GenericBuilder<GetDiagnosticDeviceList> builder() {
        return GenericBuilder.of(GetDiagnosticDeviceList::new);
    }

    protected GetDiagnosticDeviceList() {
        super(GiraOneCommand.GetDiagnosticDeviceList);
    }
}
