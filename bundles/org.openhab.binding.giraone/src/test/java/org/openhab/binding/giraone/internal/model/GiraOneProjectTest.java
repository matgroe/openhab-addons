package org.openhab.binding.giraone.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.ws.commands.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.ws.gson.GsonMapperFactory;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import com.google.gson.Gson;

class GiraOneProjectTest {

    private GiraOneProject project;

    @BeforeEach
    void setUp() {
        Gson gson = GsonMapperFactory.createGson();

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
    void testLookupChannelsByGiraOneFunctionType(GiraOneChannelTypeId type) {
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
}
