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

import java.util.ArrayList;
import java.util.Collection;

/**
 * The {@link GiraOneChannelCollection} describes a base component within the GiraOne SmartHome system.
 * A physical GiraOneComponent might Triggering Element like a Rocker or Button
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneChannelCollection {
    private final Collection<GiraOneChannel> channels = new ArrayList<>();

    public Collection<GiraOneChannel> getChannels() {
        return channels;
    }

    public void add(GiraOneChannel giraOneChannel) {
        this.channels.add(giraOneChannel);
    }
}
