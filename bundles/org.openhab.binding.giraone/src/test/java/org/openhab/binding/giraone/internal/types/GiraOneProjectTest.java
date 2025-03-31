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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
@NonNullByDefault({ DefaultLocation.PARAMETER })
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

    @DisplayName("should find existing channel by channelViewUrn")
    @Test
    void shouldFindChannelByChannelViewUrn() {
        String urn = "urn:gds:chv:KNXheating2Fcooling-Heating-Cooling-Switchable-9";
        Optional<GiraOneChannel> channel = project.lookupChannelByChannelViewUrn(urn);
        assertTrue(channel.isPresent());
        assertEquals(urn, channel.get().getChannelViewUrn());
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
    void testLookupChannelsByGiraOneChannel(GiraOneChannelType type) {
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
        Optional<GiraOneChannel> channel = project.lookupChannelByChannelUrn(urn);
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
        Optional<GiraOneChannel> channel = project.lookupChannelByChannelUrn(urn);
        assertTrue(channel.isPresent());

        Collection<GiraOneProjectItem> items = project.findReferencingGiraOneProjectItems(channel.get());
        assertFalse(items.isEmpty());
    }

    @DisplayName("should find de-reference referenced GiraOneChannels for given GiraOneProjectItem")
    @Test
    void testLookupChannels() {
        Collection<GiraOneProjectItem> items = project.lookupProjectItems(GiraOneItemMainType.Trade,
                GiraOneItemSubType.Lighting);
        assertFalse(items.isEmpty());

        GiraOneProjectItem item = items.stream().findFirst().get();
        Collection<GiraOneChannel> channels = project.lookupChannels(item);
        assertFalse(channels.isEmpty());
        assertEquals(item.getItemReferences().size(), channels.size());
    }

    @DisplayName("should find a channel by it's name")
    @ParameterizedTest
    @ValueSource(strings = { "WC Deckenlicht", "Eckfenster Bad Links" })
    void testLookupChannelByName(String name) {
        Optional<GiraOneChannel> channel = project.lookupChannelByName(name.toLowerCase());
        assertFalse(channel.isEmpty());
        assertEquals(name, channel.get().getName());
    }

    @Test
    void testLookupGiraOneChannelDataPoints() {
        GiraOneDataPoint dp = project.lookupGiraOneDataPoint(215656).orElse(null);
        assertNotNull(dp);
        assertEquals(215656, dp.getId());
        assertEquals("urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxHvacActuator6-gang-1.Heatingactuator-1:Set-Point",
                dp.getUrn());
        assertEquals("Set-Point", dp.getName());
    }
}
