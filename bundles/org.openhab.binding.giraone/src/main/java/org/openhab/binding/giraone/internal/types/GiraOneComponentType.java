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
package org.openhab.binding.giraone.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration describes type of {@link GiraOneComponent}
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
public enum GiraOneComponentType {
    KnxDimmingActuator,
    KnxHvacActuator,
    KnxSwitchingActuator,
    KnxButton,
    Unknown;

    public static GiraOneComponentType fromName(String name) {
        if (name.matches("urn:gds:cmp:.*:KnxDimmingActuator.*")) {
            return KnxDimmingActuator;
        } else if (name.matches("urn:gds:cmp:.*:KnxHvacActuator.*")) {
            return KnxHvacActuator;
        } else if (name.matches("urn:gds:cmp:.*:KnxSwitchingActuator.*")) {
            return KnxSwitchingActuator;
        } else if (name.matches("urn:gds:cmp:.*:KnxButton.*")) {
            return KnxButton;
        }
        return Unknown;
    }
}
