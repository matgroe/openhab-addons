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

package org.openhab.binding.giraone.internal.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.giraone.internal.communication.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketResponse;
import org.openhab.binding.giraone.internal.types.GiraOneChannelCollection;
import org.openhab.binding.giraone.internal.types.GiraOneDataPoint;
import org.openhab.binding.giraone.internal.types.GiraOneProject;

import com.google.gson.Gson;

/**
 * Utility provides test data for various unit tests.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class TestDataProvider {

    private static GiraOneDataPoint dataPointBuilder(final String name, final int id, final String urn) {
        return GenericBuilder.of(GiraOneDataPoint::new).with(GiraOneDataPoint::setId, id)
                .with(GiraOneDataPoint::setName, name).with(GiraOneDataPoint::setUrn, urn).build();
    }

    public static GiraOneProject createGiraOneProject() {
        Gson gson = GsonMapperFactory.createGson();

        String message = ResourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneWebsocketResponse response = gson.fromJson(message, GiraOneWebsocketResponse.class);
        assertNotNull(response);
        assertInstanceOf(GetUIConfiguration.class, response.getRequestServerCommand().getCommand());
        GiraOneChannelCollection uiChannels = response.getReply(GiraOneChannelCollection.class);

        GiraOneProject project = new GiraOneProject();
        uiChannels.getChannels().forEach(project::addChannel);

        return project;
    }

    public static GiraOneDataPoint createDataPointStepUpDown() {
        return dataPointBuilder("Step-Up-Down", 215876,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-5:Up-Down");
    }

    public static GiraOneDataPoint dataPointUpDown() {
        return dataPointBuilder("Up-Down", 215877,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-5:Step-Up-Down");
    }

    public static GiraOneDataPoint dataPointMovement() {
        return dataPointBuilder("Movement", 215878,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-5:Movement");
    }

    public static GiraOneDataPoint dataPointPosition() {
        return dataPointBuilder("Position", 215879,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-5:Position");
    }

    public static GiraOneDataPoint dataPointSlatPosition() {
        return dataPointBuilder("Step-Up-Down", 215880,
                "urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-5:Slat-Position");
    }
}
