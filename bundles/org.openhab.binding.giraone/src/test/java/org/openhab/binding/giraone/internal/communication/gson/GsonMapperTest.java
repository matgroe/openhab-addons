package org.openhab.binding.giraone.internal.communication.gson;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneEvent;
import org.openhab.binding.giraone.internal.communication.ws.GiraOneMessageType;
import org.openhab.binding.giraone.internal.communication.ws.commands.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.ws.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;
import org.openhab.binding.giraone.internal.communication.ws.gson.GsonMapperFactory;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonMapperTest {
    private ResourceLoader resourceLoader;
    private Gson gson;

    @BeforeEach
    void setUp() {
        resourceLoader = new ResourceLoader();
        gson = GsonMapperFactory.createGson();
    }

    private static Stream<Arguments> provideWebsocketMessageTypes() {
        return Stream.of(
                Arguments.of("/messages/0.Types/001-invalid-message.json", GiraOneMessageType.Invalid),
                Arguments.of("/messages/0.Types/002-response-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/002-response-ok.json", GiraOneMessageType.Response),
                Arguments.of("/messages/0.Types/003-event-error.json", GiraOneMessageType.Error),
                Arguments.of("/messages/0.Types/003-event-ok.json", GiraOneMessageType.Event)
        );
    }

    @DisplayName("message should deserialize to GiraOneMessageType")
    @ParameterizedTest
    @MethodSource("provideWebsocketMessageTypes")
    void shouldDeserialize2WebsocketMessageType(String resourceName, GiraOneMessageType expected) {
        String message = resourceLoader.loadStringResource(resourceName);
        assertEquals(expected, gson.fromJson(message, GiraOneMessageType.class));
    }

    @Test
    void shouldSerializeObjectOfRegisterApplication() {
        RegisterApplication request = RegisterApplication.builder().build();
        RegisterApplication registerApplication = gson.fromJson(gson.toJson(request, ServerCommand.class), RegisterApplication.class);
        assertEquals(request.getApplicationId(), registerApplication.getApplicationId());
        assertEquals(request.getCommand(), registerApplication.getCommand());
    }

    @DisplayName("message should deserialize to GiraOneEvent")
    @Test
    void shouldDeserialize2WebsocketEvent() {
        String message = resourceLoader.loadStringResource("/messages/0.Events/001-evt.json");

        GiraOneEvent event = gson.fromJson(message, GiraOneEvent.class);

        assertEquals("220940",event.getId());
        assertEquals("2.3:false",event.getNewInternal());
        assertEquals("2.3:true",event.getOldInternal());
        assertEquals("0",event.getNewValue());
        assertEquals("1",event.getOldValue());
        assertEquals("k:12.0.31",event.getSource());
        assertEquals("urn:gds:dp:GiraOneServer.GIOSRVKX03:KnxSwitchingActuator24-gang2C16A2FBlindActuator12-gang-1.Curtain-2:Movement",event.getUrn());
        assertEquals("Value",event.getState());

    }

    @DisplayName("message should deserialize to GiraOneCommandResponse")
    @Test
    void shouldDeserialize2GiraOneCommandResponse() {
        String message = resourceLoader.loadStringResource("/messages/2.GetUIConfiguration/001-resp.json");
        GiraOneCommandResponse response = gson.fromJson(message, GiraOneCommandResponse.class);

        assertEquals(GiraOneCommand.GetUIConfiguration, response.getRequestServerCommand().getCommand());
    }
}


