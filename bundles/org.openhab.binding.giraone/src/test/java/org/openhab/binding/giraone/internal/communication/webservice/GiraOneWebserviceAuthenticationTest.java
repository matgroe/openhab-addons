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
package org.openhab.binding.giraone.internal.communication.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Tests for {@link GiraOneWebserviceAuthentication}.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({})
public class GiraOneWebserviceAuthenticationTest {
    GiraOneWebserviceAuthentication auth;

    public GiraOneWebserviceAuthenticationTest() throws NoSuchAlgorithmException {
        auth = new GiraOneWebserviceAuthentication();
    }

    @DisplayName("Salt and Hash Password")
    @ParameterizedTest
    @CsvSource({
            "Iauvx0sXV6ljy9GbryJGJA,3AADACFB530D0721C1A1B83E62A6DC3A,1234567, rHqg4QFvcLTai33gyBCwaizZlgtOM7M01OW0qMUmuP8"

    })
    public void testSaltAndHashPassword(String salt, String sessionSalt, String password, String expected) {
        String a = auth.saltAndHashPassword(new GiraOneWebserviceSession(salt, sessionSalt, "GDS_1"), password);
        assertEquals(expected, a);
    }

    @DisplayName("Compute Authentication Token")
    @ParameterizedTest
    @CsvSource({
            "Iauvx0sXV6ljy9GbryJGJA, 994920F52F2B888FF2A51AC50975690C,!Ncc1701D, C56C61CF21680981BFB6EC4A6321E0B565457F5759BC3CE14475FB588818E1B9",
            "Iauvx0sXV6ljy9GbryJGJA, 358DCBD6F367A001070EADEEAFE84C1C,!Ncc1701D, 159F357541FB95AFCB8D489341122C3F4DC8AA87E7CE28F94D96C073FA2ACC43",
            "Iauvx0sXV6ljy9GbryJGJA, 8FC164B4782D0BBEBA4AE07ABCAB17CE,!Ncc1701D, D4C3E25DA378CB88791AEFF7025084153F064CF3D53728EE232329BA01089F00" })
    public void testComputeAuthToken(String salt, String sessionSalt, String password, String expected) {
        String token = auth.computeAuthToken(new GiraOneWebserviceSession(salt, sessionSalt, "GDS_1"), password);
        assertEquals(expected, token);
    }
}
