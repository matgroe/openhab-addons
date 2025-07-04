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

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.giraone.internal.GiraOneClientConfiguration;
import org.openhab.binding.giraone.internal.communication.GiraOneClientException;
import org.openhab.binding.giraone.internal.communication.GiraOneCommand;
import org.openhab.binding.giraone.internal.communication.GiraOneCommandResponse;
import org.openhab.binding.giraone.internal.communication.GiraOneCommunicationException;
import org.openhab.binding.giraone.internal.communication.commands.AuthenticateSession;
import org.openhab.binding.giraone.internal.communication.commands.GetDiagnosticDeviceList;
import org.openhab.binding.giraone.internal.communication.commands.GetPasswordSalt;
import org.openhab.binding.giraone.internal.types.GiraOneComponentCollection;
import org.openhab.binding.giraone.internal.util.GsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class gives access the the Gira One Server as offered by the http interface.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class GiraOneWebserviceClient {
    private static final String ERR_COMMUNICATION = "ERR_COMMUNICATION";
    private static final int ERR_COMMUNICATION_CODE = 10000;

    private static final String TEMPLATE_WEBSERVICE_URL = "http://%s/webservice";

    private final Logger logger = LoggerFactory.getLogger(GiraOneWebserviceClient.class);
    private final GiraOneClientConfiguration clientConfiguration;
    private final HttpClient.Builder clientBuilder;
    private final URI webserviceUri;
    private final Gson gson;

    /**
     * Constructor
     *
     * @param config A {@link GiraOneClientConfiguration} object
     */
    public GiraOneWebserviceClient(final GiraOneClientConfiguration config) {
        Objects.requireNonNull(config.hostname, "GiraOneClientConfiguration 'hostname' must not be null");
        Objects.requireNonNull(config.username, "GiraOneClientConfiguration 'username' must not be null");
        Objects.requireNonNull(config.password, "GiraOneClientConfiguration 'password' must not be null");
        this.clientConfiguration = config;
        this.gson = GsonMapperFactory.createGson();
        this.clientBuilder = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .proxy(ProxySelector.getDefault());
        try {
            this.webserviceUri = new URI(String.format(TEMPLATE_WEBSERVICE_URL, this.clientConfiguration.hostname));
        } catch (URISyntaxException e) {
            throw new GiraOneClientException("Cannot format webservice URI", e);
        }
    }

    /**
     * Establish a new Websocket connection to the Gira One Server.
     *
     * @throws GiraOneClientException
     */
    public void connect() throws GiraOneCommunicationException {
        this.authenticateUser(clientConfiguration.username, clientConfiguration.password);
    }

    protected String doPost(final String body) throws IOException {
        HttpRequest request = HttpRequest.newBuilder().uri(this.webserviceUri)
                .headers("Content-Type", "text/plain;charset=UTF-8").POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try (HttpClient client = clientBuilder.build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK_200) {
                throw new IOException(String.format("The HTTP Post to {%s} failed with status code {%d}",
                        this.webserviceUri, response.statusCode()));
            }
            return response.body();
        } catch (Exception e) {
            throw new IOException(String.format("Cannot post {%s} to {%s}", body, this.webserviceUri), e);
        }
    }

    /**
     * Sends a {@link GiraOneCommand} to Gira One Server and provides server's command response
     * as {@link GiraOneCommandResponse} object.
     *
     * @param command The command to send
     * @throws {{@link GiraOneCommunicationException} in case of an error.
     */
    public GiraOneCommandResponse execute(GiraOneCommand command) throws GiraOneCommunicationException {
        try {
            String message = gson.toJson(new GiraOneWebserviceRequest(command), GiraOneWebserviceRequest.class);
            logger.trace("SEND command :: {}", message);
            String body = this.doPost(message);

            logger.trace("RCV command response :: {}", body);
            JsonElement responseElement = JsonParser.parseString(body);
            if (responseElement != null && responseElement.isJsonObject()) {
                JsonObject responseObject = responseElement.getAsJsonObject();
                if (responseObject.has("error")) {
                    throw new GiraOneCommunicationException(command, responseObject.get("error").getAsString(),
                            responseObject.get("id").getAsInt());
                }
                return gson.fromJson(body, GiraOneWebserviceResponse.class);
            }
        } catch (IOException exp) {
            throw new GiraOneCommunicationException(command, exp.getMessage(), exp);
        }
        throw new GiraOneCommunicationException(command, ERR_COMMUNICATION, ERR_COMMUNICATION_CODE);
    }

    /**
     * initiates a http session on gira server and authenticates against the given user credentials
     *
     * @param username - the GiraOne User
     * @param password - the user's password
     */
    private void authenticateUser(final String username, final String password) throws GiraOneCommunicationException {
        GiraOneCommandResponse saltAsJson = this
                .execute(GetPasswordSalt.builder().with(GetPasswordSalt::setUsername, username).build());

        GiraOneWebserviceSession session = saltAsJson.getReply(GiraOneWebserviceSession.class);

        GiraOneWebserviceAuthentication auth = new GiraOneWebserviceAuthentication();
        String token = auth.computeAuthToken(session, password);

        this.execute(AuthenticateSession.builder().with(AuthenticateSession::setUsername, username)
                .with(AuthenticateSession::setToken, token).build());
    }

    public GiraOneComponentCollection lookupGiraOneComponentCollection() throws GiraOneCommunicationException {
        GiraOneCommandResponse response = this.execute(GetDiagnosticDeviceList.builder().build());
        return response.getReply(GiraOneComponentCollection.class);
    }
}
