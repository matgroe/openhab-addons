package org.openhab.binding.giraone.internal.communication.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneCommunicationException;
import org.openhab.binding.giraone.internal.communication.websocket.GiraOneWebsocketSequence;
import org.openhab.binding.giraone.internal.util.ResourceLoader;

import com.google.gson.JsonParser;

class GiraOneWebserviceClientTest {
    private GiraOneWebserviceClient giraOneWebserviceClient;
    private GiraOneClientConfiguration configuration;

    @BeforeEach
    void setUp() {
        GiraOneWebsocketSequence.reset();

        configuration = new GiraOneClientConfiguration();
        configuration.username = "User";
        configuration.password = "!Ncc1701D";
        configuration.hostname = "192.168.178.138";
        configuration.maxTextMessageSize = 350000;
        configuration.defaultTimeoutSeconds = 45;

        giraOneWebserviceClient = Mockito.spy(new GiraOneWebserviceClient(configuration));
    }

    @DisplayName("Should authenticate against gira one server")
    @Test
    void testWebserviceAuthentication() throws Exception {
        String response = ResourceLoader.loadStringResource("/messages/8.GetPasswordSalt/001-resp.json");

        Mockito.doReturn(response).when(giraOneWebserviceClient).doPost(Mockito.any());
        giraOneWebserviceClient.connect();

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(giraOneWebserviceClient, times(2)).doPost(argumentCaptor.capture());
        String[] args = argumentCaptor.getAllValues().toArray(new String[0]);

        assertEquals("getPasswordSalt", JsonParser.parseString(args[0]).getAsJsonObject().get("command").getAsString());
        assertEquals(configuration.username, JsonParser.parseString(args[0]).getAsJsonObject().getAsJsonObject("data")
                .get("username").getAsString());

        assertEquals("doAuthenticateSession",
                JsonParser.parseString(args[1]).getAsJsonObject().get("command").getAsString());
        assertEquals("EC1D6D0C2CCEAD3C3A6CD0536FE1E4968AF3BAE59DBF9730FE36338A7B03DB7D",
                JsonParser.parseString(args[1]).getAsJsonObject().getAsJsonObject("data").get("token").getAsString());
    }

    @DisplayName("Should fail with GiraOneCommunicationException")
    @Test
    void testWebserviceAuthenticationFails() throws Exception {
        String response = ResourceLoader.loadStringResource("/messages/8.GetPasswordSalt/002-resp.json");

        Mockito.doReturn(response).when(giraOneWebserviceClient).doPost(Mockito.any());
        GiraOneCommunicationException thrown = assertThrows(GiraOneCommunicationException.class,
                () -> giraOneWebserviceClient.connect(),
                "Expected giraOneWebserviceClient.connect() to throw GiraOneCommunicationException, but it didn't");
        assertEquals("ERR_COMMUNICATION.10000", thrown.getMessage());
    }
}
