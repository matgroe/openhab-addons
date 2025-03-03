package org.openhab.binding.giraone.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
public class GiraOneClientConnectionTest {
    private GiraOneClientConfiguration configuration = new GiraOneClientConfiguration();
    private GiraOneClient giraClient = new GiraOneClient(configuration);

    @BeforeEach
    void setUp() {
        configuration.username = "User";
        configuration.password = "Ncc1701D";
        configuration.hostname = "192.168.178.38";
        configuration.maxTextMessageSize = 350000;
        configuration.defaultTimeoutSeconds = 45;
    }

    @Test
    void testConnectWithInvalidCredentials() {
        configuration.password = "_invalid_";
        giraClient = new GiraOneClient(configuration);
        giraClient.connect();
    }

    @Test
    void testConnectWithInvalidHostname() {
        configuration.hostname = "127.0.0.1";
        giraClient = new GiraOneClient(configuration);
        giraClient.connect();
    }

    @Test
    void testConnectWithInvalidTextMessageSize() {
        configuration.maxTextMessageSize = 20;
        giraClient = new GiraOneClient(configuration);
        giraClient.connect();
    }
}
