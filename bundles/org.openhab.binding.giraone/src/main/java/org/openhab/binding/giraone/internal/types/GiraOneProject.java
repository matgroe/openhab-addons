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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This class represents the project installation within your as configured GiraOne SmartHome
 * Environment. It offers some functions for accessing the {@link GiraOneChannel} and
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneProject {

    private final Set<GiraOneChannel> channels = Collections.synchronizedSet(new HashSet<>());

    /**
     * Constructor.
     */
    public GiraOneProject() {
    }

    /**
     * Adds the given channel to it's Set of {@link GiraOneChannel}. Duplicates
     * with same urn are getting ignored.
     *
     * @param channel The {@link GiraOneChannel} to add.
     */
    public void addChannel(GiraOneChannel channel) {
        this.channels.add(channel);
    }

    /**
     * @return Returna a {@link Collection} of all {@link GiraOneChannel} within this project.
     */
    public Collection<GiraOneChannel> lookupChannels() {
        return channels;
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneChannel}
     * by the given channelUrn.
     *
     * @param urn The channelUrn
     * @return The optional {@link GiraOneChannel}, if there is any
     */
    public Optional<GiraOneChannel> lookupChannelByUrn(final String urn) {
        return this.channels.stream().filter(f -> urn.equals(f.getUrn())).findFirst();
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneChannel}
     * by the given channel name.
     *
     * @param name The channel name
     * @return The optional {@link GiraOneChannel}, if there is any
     */
    public Optional<GiraOneChannel> lookupChannelByName(final String name) {
        return this.channels.stream().filter(f -> name.equalsIgnoreCase(f.getName())).findFirst();
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneChannel}
     * by the given channelViewId.
     *
     * @param channelViewId The channelViewId
     * @return A {@link Optional} of {@link GiraOneChannel}, if there is any
     */
    public Optional<GiraOneChannel> lookupChannelByChannelViewId(final int channelViewId) {
        return this.channels.stream().filter(f -> f.getChannelViewId() == channelViewId).findFirst();
    }

    /**
     * This method returns a collection of {@link GiraOneChannel} the given {@link GiraOneDataPoint} is assigned to.
     *
     * @param dataPoint - The {@link GiraOneDataPoint} to assign on it's referenced channel
     * @return The {@link Collection} of {@link GiraOneChannel} containing the given {@link GiraOneDataPoint}
     */
    public Collection<GiraOneChannel> lookupGiraOneChannels(GiraOneDataPoint dataPoint) {
        return this.channels.stream().filter(ch -> ch.containsGiraOneDataPoint(dataPoint.getId())).toList();
    }

    /**
     * This method iterates searches over all channels for the given dataPointId and returns the
     * concerning {@link GiraOneDataPoint} if there is any.
     *
     * @param dataPointId - The datapoint identifier
     * @return A {@link Optional} of {@link GiraOneDataPoint}
     */
    public Optional<GiraOneDataPoint> lookupGiraOneDataPoint(final int dataPointId) {
        return this.channels.stream().filter(ch -> ch.containsGiraOneDataPoint(dataPointId))
                .map(GiraOneChannel::getDataPoints).flatMap(Collection::stream).filter(x -> x.getId() == dataPointId)
                .findFirst();
    }
}
