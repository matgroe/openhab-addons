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
package org.openhab.binding.giraone.internal.dto;

import org.openhab.binding.giraone.internal.util.GenericBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents the project installation within your as configured GiraOne SmartHome
 * Environment. It offers some functions for accessing the {@link GiraOneProjectChannel} and
 * {@link GiraOneProjectItem} objects that describes the setup.
 *
 * @author Matthias Gröger - Initial contribution
 */
public class GiraOneProject {
    private final GiraOneProjectItem root;
    private final Collection<GiraOneProjectChannel> channels;

    public static GiraOneProject empty() {
        return new GiraOneProject(new GiraOneProjectItem(), new ArrayList<>());
    }

    /**
     * Constructor.
     *
     * @param root The root element
     * @param channels All configured channels
     */
    public GiraOneProject(GiraOneProjectItem root, Collection<GiraOneProjectChannel> channels) {
        this.root = root;
        this.channels = channels;
    }

    /**
     * Converts the {@link GiraOneProjectItem} hierarchy into stream that holds
     * all referenced children within the created stream.
     * 
     * @param item The root item to flatten.
     * @return A {@link Stream} of referenced GiraOneProjectItems.
     */
    private Stream<GiraOneProjectItem> flatten(GiraOneProjectItem item) {
        return Stream.concat(Stream.of(item), item.getChildren().stream().flatMap(this::flatten)); // recursion here
    }

    /**
     * Creates an instance of {@link GiraOneItemReference} from the given refId.
     * 
     * @param refId The referenceId.
     * @return {@link GiraOneItemReference}
     */
    private GiraOneItemReference makeGiraOneItemReference(int refId) {
        return new GiraOneItemReference() {
            @Override
            public int getReferenceId() {
                return refId;
            }
        };
    }

    /**
     * @return Returna a {@link Collection} of all {@link GiraOneProjectChannel} within this project.
     */
    public Collection<GiraOneProjectChannel> lookupChannels() {
        return channels;
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel} by the given
     * filter of {@link GiraOneFunctionType}.
     *
     * @param type The {@link GiraOneFunctionType} to filter against.
     * @return A {@link Collection} of matching {@link GiraOneFunctionType}
     */
    public Collection<GiraOneProjectChannel> lookupChannels(GiraOneFunctionType type) {
        return channels.stream().filter(x -> x.getFunctionType() == type).collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel} by the given
     * filter of {@link GiraOneChannelType}.
     *
     * @param type The {@link GiraOneChannelType} to filter against.
     * @return A {@link Collection} of matching {@link GiraOneChannelType}
     */
    public Collection<GiraOneProjectChannel> lookupChannels(GiraOneChannelType type) {
        return channels.stream().filter(x -> x.getChannelType() == type).collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel} by the given
     * filter of {@link GiraOneChannelTypeId}.
     *
     * @param type The {@link GiraOneChannelTypeId} to filter against.
     * @return A {@link Collection} of matching {@link GiraOneChannelTypeId}
     */
    public Collection<GiraOneProjectChannel> lookupChannels(GiraOneChannelTypeId type) {
        return channels.stream().filter(x -> x.getChannelTypeId() == type).collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectItem} by the given
     * filter of {@link GiraOneItemMainType}.
     *
     * @param mainType The {@link GiraOneItemMainType} to filter against.
     * @return A {@link Collection} of matching {@link GiraOneItemMainType}
     */
    public Collection<GiraOneProjectItem> lookupProjectItems(GiraOneItemMainType mainType) {
        return this.flatten(root).filter(f -> mainType == f.getMainType()).collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectItem} by the given
     * filter of {@link GiraOneItemMainType} and {@link GiraOneItemSubType}.
     *
     * @param mainType The {@link GiraOneItemMainType} to filter against.
     * @param subType The {@link GiraOneItemSubType} to filter against.
     * @return A {@link Collection} of matching {@link GiraOneItemMainType}
     */
    public Collection<GiraOneProjectItem> lookupProjectItems(GiraOneItemMainType mainType, GiraOneItemSubType subType) {
        return lookupProjectItems(mainType).stream().filter(f -> subType == f.getSubType())
                .collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectItem} by the given
     * {@link GiraOneProjectChannel}
     * and returns a {@link Collection} of {@link GiraOneProjectItem} which are referencing the given
     *
     * @param channel The referencing {@link GiraOneProjectChannel}
     * @return a {@link Collection} of {@link GiraOneProjectItem}
     */
    public Collection<GiraOneProjectItem> findReferencingGiraOneProjectItems(GiraOneProjectChannel channel) {
        return this.flatten(root).filter(f -> f.isReferencing(makeGiraOneItemReference(channel.getChannelViewId())))
                .collect(Collectors.toList());
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectItem} having a mainType
     * of GiraOneItemMainType.Location and is referencing the given {@link GiraOneProjectChannel}
     * 
     * @param channel The {@link GiraOneProjectChannel}
     * @return The concerning {@link GiraOneProjectItem} which represents the {@link GiraOneProjectChannel}'s location
     */
    public Optional<GiraOneProjectItem> findLocationForChannel(GiraOneProjectChannel channel) {
        GiraOneItemReference ref = makeGiraOneItemReference(channel.getChannelViewId());
        return this.flatten(root).filter(x -> x.getMainType() == GiraOneItemMainType.Location)
                .filter(f -> f.isReferencing(ref)).findFirst();
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel}
     * by the given channelViewUrn.
     * 
     * @param urn The channelViewUrn
     * @return The optional {@link GiraOneProjectChannel}, if there is any
     */
    public Optional<GiraOneProjectChannel> lookupChannelByChannelViewUrn(final String urn) {
        return this.channels.stream().filter(f -> urn.equals(f.getChannelViewUrn())).findFirst();
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel}
     * by the given channelUrn.
     *
     * @param urn The channelUrn
     * @return The optional {@link GiraOneProjectChannel}, if there is any
     */
    public Optional<GiraOneProjectChannel> lookupChannelByChannelUrn(final String urn) {
        return this.channels.stream().filter(f -> urn.equals(f.getChannelUrn())).findFirst();
    }

    /**
     * Performs a lookup within the internal {@link Collection} of {@link GiraOneProjectChannel}
     * by the given channelViewId.
     *
     * @param channelViewId The channelViewId
     * @return A {@link Optional} of {@link GiraOneProjectChannel}, if there is any
     */
    public Optional<GiraOneProjectChannel> lookupChannelByChannelViewId(final int channelViewId) {
        return this.channels.stream().filter(f -> f.getChannelViewId() == channelViewId).findFirst();
    }

    /**
     * This method takes {@link GiraOneProjectItem} and dereferences the internal channel references to the
     * concerning {@link GiraOneProjectChannel} objects.
     * 
     * @param item The item to dereference
     * @return a {@link Collection} of {@link GiraOneProjectChannel}
     */
    public Collection<GiraOneProjectChannel> lookupProjectChannels(final GiraOneProjectItem item) {
        Collection<Integer> refIds = item.getItemReferences().stream().map(GiraOneItemReference::getReferenceId)
                .toList();
        return this.channels.stream().filter(f -> refIds.contains(f.getChannelViewId())).collect(Collectors.toList());
    }

    /**
     * Creates a {@link GiraOneChannelDataPoint} from the given {@link GiraOneDataPoint} and
     * {@link GiraOneProjectChannel}
     * objects. Missing data is getting enriched to the destination object, if it's available.
     *
     * @param projectChannel - The {@link GiraOneChannelDataPoint}
     * @param dataPoint - The {@link GiraOneDataPoint}
     *
     * @return the {@link GiraOneChannelDataPoint}
     */
    private GiraOneChannelDataPoint createGiraOneChannelDataPoint(GiraOneProjectChannel projectChannel,
            GiraOneDataPoint dataPoint) {

        GenericBuilder<GiraOneChannelDataPoint> builder =  GenericBuilder.of(GiraOneChannelDataPoint::new);

        return builder.with(GiraOneChannelDataPoint::setChannelViewId, projectChannel.getChannelViewId())
                .with(GiraOneChannelDataPoint::setChannelViewUrn,projectChannel.getChannelViewUrn())
                .with(GiraOneChannelDataPoint::setGiraOneDataPoint, dataPoint).build();

    }

    /**
     * This method iterates through over all channels and creates new {@link GiraOneChannelDataPoint} object which
     * combines all relevant information about the given datapoint and it's assigned channels.
     *
     * @param dataPoint - The {@link GiraOneDataPoint} to assign on it's referenced channel
     * @return A {@link Collection} of {@link GiraOneChannelDataPoint}
     */
    public Collection<GiraOneChannelDataPoint> lookupGiraOneChannelDataPoints(GiraOneDataPoint dataPoint) {
        return this.channels.stream().filter(ch -> ch.containsGiraOneDataPoint(dataPoint.getId()))
                .map(ch -> createGiraOneChannelDataPoint(ch, dataPoint)).toList();
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
                .map(GiraOneProjectChannel::getDataPoints).flatMap(Collection::stream)
                .filter(x -> x.getId() == dataPointId).findFirst();
    }
}
