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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link GiraOneURN}
 *
 * @author Matthias Gröger - Initial contribution
 */
class GiraOneURNTest {

    @DisplayName("Should accept all given Strings and return them as urn")
    @ParameterizedTest
    @ValueSource(strings = { "ssrn:gds:chv:NumericFloatingPointStatus-Float-7",
            "ursn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Humidity-1:HumidityStatus" })
    void shouldThrowIllegalArgumentException(String urn) {
        assertThrows(IllegalArgumentException.class, () -> GiraOneURN.of(urn));
    }

    @DisplayName("Should accept all given Strings and return them as urn")
    @ParameterizedTest
    @ValueSource(strings = { "urn:gds:chv:NumericFloatingPointStatus-Float-7",
            "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Humidity-1:HumidityStatus" })
    void shouldAcceptAllGivenStrings(String urn) {
        assertEquals(urn, GiraOneURN.of(urn).toString());
    }

    @DisplayName("Should provide parent urn")
    @Test
    void shouldGiveParentUrn() {
        assertEquals("urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Humidity-1",
                GiraOneURN.of(
                        "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Humidity-1:HumidityStatus")
                        .getParent().toString());
    }

    @DisplayName("Should provide resource name")
    @Test
    void shouldGiveResourceName() {
        assertEquals("HumidityStatus", GiraOneURN.of(
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxButton4Comfort2CSystem55Rocker2-gang-3.Humidity-1:HumidityStatus")
                .getResourceName());
    }
}
