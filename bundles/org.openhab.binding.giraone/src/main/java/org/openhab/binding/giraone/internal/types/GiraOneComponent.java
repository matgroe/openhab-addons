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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.List;

/**
 * The {@link GiraOneComponent} describes a base component within the GiraOne SmartHome system.
 * A physical GiraOneComponent might Triggering Element like a Rocker or Button
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneComponent {
    private String name;
    private String urn;
    private GiraOneComponentType type;
    private List<GiraOneChannel> channels = List.of();

    public GiraOneComponent(String name, String urn) {
        this.name = name;
        this.urn = urn;
    }

    public GiraOneComponentType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getUrn() {
        return urn;
    }

    public List<GiraOneChannel> getChannels() {
        return channels;
    }
}
