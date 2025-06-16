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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketResponse;
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
        GiraOneWebsocketResponse response = gson.fromJson(message, GiraOneWebsocketResponse.class);
        assertNotNull(response);
        assertInstanceOf(GetUIConfiguration.class, response.getRequestServerCommand().getCommand());

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
