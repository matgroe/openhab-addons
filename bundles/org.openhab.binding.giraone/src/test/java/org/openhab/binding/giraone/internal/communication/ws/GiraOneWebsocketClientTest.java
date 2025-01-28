package org.openhab.binding.giraone.internal.communication.ws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.giraone.internal.GiraOneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.*;


class GiraOneWebsocketClientTest {

    private GiraOneConfiguration configuration;
    private GiraOneWebsocketClient wsClient;

    @BeforeEach
    void setUp() {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ((ch.qos.logback.classic.Logger) root).setLevel(Level.INFO);

        configuration = new GiraOneConfiguration();
        configuration.username = "User";
        configuration.password = "!Ncc1701D";
        configuration.hostname = "192.168.178.38";

        wsClient = new GiraOneWebsocketClient(configuration.hostname, configuration.username, configuration.password);
    }

    @DisplayName("Compute Websocket Authentication Token")
    @ParameterizedTest
    @CsvSource({
            "Blah, 1q2w3e4r5t, uiQmxhaDoxcTJ3M2U0cjV0",
            "User, Pass!Word, uiVXNlcjpQYXNzIVdvcmQ="
    })
    public void testComputeWebsocketAuthToken(String username, String password, String expected) {
        String token = wsClient.computeWebsocketAuthToken(username, password);
        assertEquals(expected, token);
    }

    @Test
    void testConnect() throws Exception {
        wsClient.connect();
        Thread.sleep(10000);
    }

}