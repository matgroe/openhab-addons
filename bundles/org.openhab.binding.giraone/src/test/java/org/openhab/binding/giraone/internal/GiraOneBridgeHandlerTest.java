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
package org.openhab.binding.giraone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.thing.Bridge;

/**
 * Test class for {@link GiraOneBridgeHandler}
 * 
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
class GiraOneBridgeHandlerTest {
    private Bridge bridge = Mockito.mock(Bridge.class);
    // private GiraOneBridgeHandler bridgeHandler = new GiraOneBridgeHandler(bridge);

    @BeforeEach
    void setUp() {
        // bridge = Mockito.mock(Bridge.class);
        // bridgeHandler = new GiraOneBridgeHandler(bridge);
    }

    @Test
    void handleCommand() {
    }

    @Test
    void initialize() {
        // bridgeHandler.initialize();
    }
}
