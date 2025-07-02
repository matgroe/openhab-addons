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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.giraone.internal.util.TestDataProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link GiraOneProject}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
class GiraOneProjectTest {

    @DisplayName("should find existing channel by channelViewUrn")
    @Test
    void shouldFindChannelByChannelViewUrn() {
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        String urn = "urn:gds:chv:KNXheating2Fcooling-Heating-Cooling-Switchable-9";
        Optional<GiraOneChannel> channel = project.lookupChannelByUrn(urn);
        assertTrue(channel.isPresent());
        assertEquals(urn, channel.get().getUrn());
    }

    @DisplayName("should find a channel by it's name")
    @ParameterizedTest
    @ValueSource(strings = { "WC Deckenlicht", "Eckfenster Bad Links" })
    void testLookupChannelByName(String name) {
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        Optional<GiraOneChannel> channel = project.lookupChannelByName(name.toLowerCase());
        assertFalse(channel.isEmpty());
        assertEquals(name, channel.get().getName());
    }

    @Test
    void testLookupGiraOneChannelDataPoints() {
        String urn = "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxHvacActuator6-gang-1.Heatingactuator-1:Set-Point";
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        GiraOneDataPoint dp = project.lookupGiraOneDataPoint(urn).orElse(null);
        assertNotNull(dp);
        assertEquals("urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxHvacActuator6-gang-1.Heatingactuator-1:Set-Point",
                dp.getUrn());
        assertEquals("Set-Point", dp.getName());
    }

    @Test
    void testLookupWildcardGiraOneChannelDataPoints() {
        String urn = "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-5.Switching-2:Feedback";
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        GiraOneDataPoint dp = project.lookupGiraOneDataPoint(urn).orElse(null);
        assertNotNull(dp);
        assertEquals("Wildcard", dp.getName());
    }

    @DisplayName("should store no GiraOneChannel duplicates")
    @Test
    void shouldStoreNoDuplicateChannels() {
        String urn = "urn:gds:chv:KNXheating2Fcooling-Heating-Cooling-Switchable-9";

        GiraOneProject project = new GiraOneProject();
        project.addChannel(TestDataProvider.createGiraOneChannel(urn));
        assertEquals(1, project.lookupChannels().size());

        project.addChannel(TestDataProvider.createGiraOneChannel(urn));
        assertEquals(1, project.lookupChannels().size());

        project.addChannel(TestDataProvider.createGiraOneChannel(urn + "1"));
        assertEquals(2, project.lookupChannels().size());
    }

    @DisplayName("should store no GiraOneChannel duplicates")
    @Test
    void shouldStoreNoDuplicateChannelsYYY() {
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        project.lookupChannels().stream().map(c -> String.format("%s, %s, %s", c.getChannelType().getName(),
                c.getChannelTypeId().getName(), c.getFunctionType().getName())).forEach(System.out::println);
    }
}
