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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.types.GiraOneChannel;
import org.openhab.binding.giraone.internal.types.GiraOneChannelType;
import org.openhab.binding.giraone.internal.types.GiraOneChannelTypeId;
import org.openhab.binding.giraone.internal.types.GiraOneFunctionType;
import org.openhab.binding.giraone.internal.types.GiraOneProject;
import org.openhab.binding.giraone.internal.util.TestDataProvider;
import org.openhab.binding.giraone.internal.util.ThingDescriptionSchemaValidationTest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.ctc.wstx.shaded.msv_core.verifier.jaxp.DocumentBuilderFactoryImpl;

/**
 * Test class for {@link GiraOneThingDiscoveryService}.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault({})
class GiraOneThingDiscoveryServiceTest {
    private GiraOneThingDiscoveryService discoveryService = spy(GiraOneThingDiscoveryService.class);

    @BeforeEach
    void setUp() {
        GiraOneProject project = TestDataProvider.createGiraOneProject();
        Configuration clientCfg = new Configuration();
        clientCfg.put("discoverDevices", true);

        GiraOneBridgeHandler thingHandler = mock(GiraOneBridgeHandler.class,
                Mockito.withSettings().extraInterfaces(Bridge.class));
        Thing thing = mock(Thing.class, Mockito.withSettings().extraInterfaces(Bridge.class));

        when(thing.getConfiguration()).thenReturn(clientCfg);

        when(thingHandler.lookupGiraOneProject()).thenReturn(project);

        when(thing.getUID()).thenReturn(
                new ThingUID(GiraOneBindingConstants.BINDING_ID, GiraOneBindingConstants.BRIDGE_TYPE_ID, "junit"));
        when(thingHandler.getThing()).thenReturn((Bridge) thing);

        discoveryService = spy(GiraOneThingDiscoveryService.class);
        discoveryService.setThingHandler(thingHandler);
        discoveryService.initialize();
    }

    private static Stream<Arguments> provideForTestDetectThingTypeUid() {
        return Stream.of(
                Arguments.of(GiraOneFunctionType.Covering, GiraOneChannelType.Shutter, GiraOneChannelTypeId.RoofWindow,
                        GiraOneBindingConstants.SHUTTER_ROOF_WINDOW_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Covering, GiraOneChannelType.Shutter, GiraOneChannelTypeId.Awning,
                        GiraOneBindingConstants.SHUTTER_AWNING_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Covering, GiraOneChannelType.Shutter,
                        GiraOneChannelTypeId.VenetianBlind, GiraOneBindingConstants.SHUTTER_VENETIAN_BLIND_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Covering, GiraOneChannelType.Unknown,
                        GiraOneChannelTypeId.VenetianBlind, GiraOneBindingConstants.GENERIC_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Light, GiraOneChannelType.Dimmer, GiraOneChannelTypeId.Light,
                        GiraOneBindingConstants.DIMMER_TYPE_ID),
                Arguments.of(GiraOneFunctionType.HeatingCooling, GiraOneChannelType.Heating,
                        GiraOneChannelTypeId.Underfloor, GiraOneBindingConstants.HEATING_COOLING_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Status, GiraOneChannelType.Status, GiraOneChannelTypeId.Temperature,
                        GiraOneBindingConstants.TEMPERATURE_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Status, GiraOneChannelType.Status, GiraOneChannelTypeId.Humidity,
                        GiraOneBindingConstants.HUMIDITY_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Status, GiraOneChannelType.Status, GiraOneChannelTypeId.PowerOutlet,
                        GiraOneBindingConstants.GENERIC_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Scene, GiraOneChannelType.Function, GiraOneChannelTypeId.Scene,
                        GiraOneBindingConstants.SCENE_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Switch, GiraOneChannelType.Switch, GiraOneChannelTypeId.Lamp,
                        GiraOneBindingConstants.SWITCH_LAMP_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Switch, GiraOneChannelType.Switch, GiraOneChannelTypeId.PowerOutlet,
                        GiraOneBindingConstants.SWITCH_POWER_OUTLET_TYPE_ID),
                Arguments.of(GiraOneFunctionType.Unknown, GiraOneChannelType.Unknown, GiraOneChannelTypeId.Unknown,
                        GiraOneBindingConstants.GENERIC_TYPE_ID));
    }

    @DisplayName("test the mapping from GiraOneChannel to ThingTypeUID")
    @ParameterizedTest
    @MethodSource("provideForTestDetectThingTypeUid")
    void testDetectThingTypeUid(GiraOneFunctionType functionType, GiraOneChannelType channelType,
            GiraOneChannelTypeId channelTypeId, String expected) {
        GiraOneChannel channel = mock(GiraOneChannel.class);
        when(channel.getChannelTypeId()).thenReturn(channelTypeId);
        when(channel.getChannelType()).thenReturn(channelType);
        when(channel.getFunctionType()).thenReturn(functionType);

        ThingTypeUID thingTypeUID = discoveryService.detectThingTypeUID(channel);

        assertEquals(expected, thingTypeUID.getId());
    }

    private static ArrayList<File> provideThingDescriptionFiles() throws IOException {
        String path = Objects.requireNonNull(ThingDescriptionSchemaValidationTest.class.getResource("/OH-INF/thing"))
                .getPath();
        ArrayList<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(path))) {
            stream.filter(Files::isRegularFile).map(Path::toFile).forEach(files::add);
        }
        return files;
    }

    private boolean checkThingTypeIdDefinitionExists(String expectedThingTypeId) throws Exception {
        boolean thingTypeIdExists = false;
        for (File file : provideThingDescriptionFiles()) {
            DocumentBuilderFactory builderFactory = new DocumentBuilderFactoryImpl();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(file);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = String.format(".//thing-type[@id=\"%s\"]", expectedThingTypeId);
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            thingTypeIdExists = thingTypeIdExists || (nodeList.getLength() > 0);

        }
        return thingTypeIdExists;
    }

    @DisplayName("There must be a thing-type definition for each determined thingTypeId")
    @ParameterizedTest
    @MethodSource("provideForTestDetectThingTypeUid")
    void testForExistingThingTypeDefinition(GiraOneFunctionType functionType, GiraOneChannelType channelType,
            GiraOneChannelTypeId channelTypeId, String expected) throws Exception {
        GiraOneChannel channel = mock(GiraOneChannel.class);
        when(channel.getChannelType()).thenReturn(channelType);
        when(channel.getChannelTypeId()).thenReturn(channelTypeId);
        when(channel.getFunctionType()).thenReturn(functionType);

        ThingTypeUID thingTypeUID = discoveryService.detectThingTypeUID(channel);

        assertTrue(this.checkThingTypeIdDefinitionExists(expected),
                "There must be thing-type definition for " + thingTypeUID.getId());
    }
}
