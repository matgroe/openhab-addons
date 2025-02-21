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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.commands.GiraOneCommand;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import com.google.gson.Gson;

/**
 * Test class for {@link GiraOneProject}.
 *
 * @author Matthias Groeger - Initial contribution
 */
class GiraOneProjectTest {
    private final Gson gson = GsonMapperFactory.createGson();

    private GiraOneProject project;

    @BeforeEach
    void setUp() {
        String message = ResourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);
        assertNotNull(response);
        assertEquals(GiraOneCommand.GetUIConfiguration, response.getRequestServerCommand().getCommand());

        project = response.getReply(GiraOneProject.class);
        assertNotNull(project);
    }

    @DisplayName("should find existing channel by urn")
    @Test
    void shouldFindChannelByUrn() {
        String urn = "urn:gds:chn:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Switching-23";
        Optional<GiraOneProjectChannel> channel = project.lookupChannelByChannelUrn(urn);
        assertTrue(channel.isPresent());
        assertEquals(urn, channel.get().getChannelUrn());
    }

    @DisplayName("should lookup channels by GiraOneFunctionType")
    @ParameterizedTest
    @EnumSource(GiraOneFunctionType.class)
    void testLookupChannelsByGiraOneFunctionType(GiraOneFunctionType type) {
        project.lookupChannels(type).forEach(x -> assertEquals(type, x.getFunctionType()));
    }

    @DisplayName("message should deserialize to GiraOneChannelType")
    @ParameterizedTest
    @EnumSource(GiraOneChannelType.class)
    void testLookupChannelsByGiraOneProjectChannel(GiraOneChannelType type) {
        project.lookupChannels(type).forEach(x -> assertEquals(type, x.getChannelType()));
    }

    @DisplayName("message should deserialize to GiraOneChannelTypeId")
    @ParameterizedTest
    @EnumSource(GiraOneChannelTypeId.class)
    void testLookupChannelsByGiraOneChannelTypeId(GiraOneChannelTypeId type) {
        project.lookupChannels(type).forEach(x -> assertEquals(type, x.getChannelTypeId()));
    }

    @DisplayName("should find location for given channel")
    @Test
    void testFindLocationForChannel() {
        String urn = "urn:gds:chn:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Switching-23";
        Optional<GiraOneProjectChannel> channel = project.lookupChannelByChannelUrn(urn);
        assertTrue(channel.isPresent());

        Optional<GiraOneProjectItem> item = project.findLocationForChannel(channel.get());
        assertTrue(item.isPresent());
        assertEquals(GiraOneItemMainType.Location, item.get().getMainType());
    }

    @DisplayName("message should lookup  to GiraOneItemMainType")
    @ParameterizedTest
    @EnumSource(GiraOneItemMainType.class)
    void testProjectItemsByGiraOneItemMainType(GiraOneItemMainType type) {
        project.lookupProjectItems(type).forEach(x -> assertEquals(type, x.getMainType()));
    }

    @DisplayName("should find referencing project items for given channel")
    @Test
    void testFindReferencingGiraOneProjectItems() {
        String urn = "urn:gds:chn:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Switching-23";
        Optional<GiraOneProjectChannel> channel = project.lookupChannelByChannelUrn(urn);
        assertTrue(channel.isPresent());

        Collection<GiraOneProjectItem> items = project.findReferencingGiraOneProjectItems(channel.get());
        assertFalse(items.isEmpty());
        // items.forEach(i ->
        // assertTrue(i.getItemReferences().contains(project.makeGiraOneItemReference(channel.get().getChannelViewId()))));
    }

    @DisplayName("should find de-reference referenced GiraOneProjectChannels for given GiraOneProjectItem")
    @Test
    void testLookupProjectChannels() {
        Collection<GiraOneProjectItem> items = project.lookupProjectItems(GiraOneItemMainType.Trade,
                GiraOneItemSubType.Lighting);
        assertFalse(items.isEmpty());

        GiraOneProjectItem item = items.stream().findFirst().get();
        Collection<GiraOneProjectChannel> channels = project.lookupProjectChannels(item);
        assertFalse(channels.isEmpty());
        assertEquals(item.getItemReferences().size(), channels.size());
    }

    private static Stream<Arguments> provideDataPointId2ChannelViewId() {
        return Stream.of(Arguments.of(215820, 216214,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator16-gang2C16A2FBlindActuator8-gang-1.Curtain-5:Step-Up-Down"),
                Arguments.of(215821, 216214,
                        "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator16-gang2C16A2FBlindActuator8-gang-1.Curtain-5:Up-Down"),
                Arguments.of(215822, 216214,
                        "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator16-gang2C16A2FBlindActuator8-gang-1.Curtain-5:Movement"));
    }

    @DisplayName("message should deserialize to GiraOneCommandResponse with GiraOneProcessView")
    @ParameterizedTest
    @MethodSource("provideDataPointId2ChannelViewId")
    void testEnrichDatapointState(int dataPointId, int channelViewId, String urn) {
        String message = ResourceLoader.loadStringResource("/messages/3.GetProcessView/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);
        assertNotNull(response);
        assertEquals(GiraOneCommand.GetProcessView, response.getRequestServerCommand().getCommand());
        GiraOneProcessView processView = response.getReply(GiraOneProcessView.class);
        assertNotNull(processView);

        Collection<GiraOneChannelDataPoint> list = processView.getDatapoints().stream()
                .map(project::enrichChannelDataPoint).collect(Collectors.toList());
        Optional<GiraOneChannelDataPoint> state = list.stream().filter(f -> f.getId() == dataPointId).findFirst();
        assertTrue(state.isPresent());
        assertEquals(channelViewId, state.get().getChannelViewId());
        assertEquals(urn, state.get().getChannelViewUrn());
    }
}
