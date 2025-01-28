package org.openhab.binding.giraone.internal.communication.ws;

import com.google.gson.Gson;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import org.openhab.binding.giraone.internal.communication.GiraOneServerClient;
import org.openhab.binding.giraone.internal.communication.ws.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;
import org.openhab.binding.giraone.internal.communication.ws.gson.GsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.*;

public class GiraOneWebsocketClient implements GiraOneServerClient, WebSocketListener {
    private final Logger logger = LoggerFactory.getLogger(GiraOneWebsocketClient.class);

    private final static String TEMPLATE_WEBSOCKET_URL = "wss://%s:4432/gds/api?%s";

    private final String giraOneWssEndpoint;
    private final Gson gson;

    private WebSocketClient webSocketClient;
    private Session websocketSession;
    private final PublishSubject<GiraOneCommandResponse> responses = PublishSubject.create();
    private final PublishSubject<GiraOneEvent> events = PublishSubject.create();

    PublishSubject<String> subject = PublishSubject.create();

    /**
     * Constructor
     *
     * @param host Gira One Server's hostname or IP
     * @param username The username for accessing the Gira functionality
     * @param password The username's password
     */
    public GiraOneWebsocketClient(final String host, final String username, final String password) {
        Objects.requireNonNull(host, "Argument 'host' must not be null");
        Objects.requireNonNull(username, "Argument 'username' must not be null");
        Objects.requireNonNull(password, "Argument 'password' must not be null");

        this.gson = GsonMapperFactory.createGson();
        this.giraOneWssEndpoint = String.format(TEMPLATE_WEBSOCKET_URL, host, computeWebsocketAuthToken(username, password));
    }

    String computeWebsocketAuthToken(String username, String password)  {
        String auth = String.format("%s:%s", username, password);
        return "ui" + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
    }

    public void connect()  {
        logger.info("Connecting to {}", this.giraOneWssEndpoint);
        HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        this.webSocketClient = new WebSocketClient(httpClient);

        try {
            webSocketClient.start();
            Future<Session> clientSessionPromise = webSocketClient.connect(this, URI.create(giraOneWssEndpoint));
            this.websocketSession = clientSessionPromise.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("connection initiated");
    }

    public void disconnect()  {
        //this.session.close();
    }

    GiraOneCommandResponse execute(ServerCommand command) {
        final CompletableFuture<GiraOneCommandResponse> promise = new CompletableFuture<>();
        Disposable disposable = Disposable.empty();
        logger.info("execute GiraOneCommand :: {}", command.getCommand());
        try {
            disposable = this.responses.filter(f -> f.getRequestServerCommand().getCommand().equals(command.getCommand()))
                    .subscribe(promise::complete);

            websocketSession.getRemote().sendString(gson.toJson(command, ServerCommand.class));
            return promise.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException | IOException exp) {
            throw new RuntimeException(exp);
        } finally {
            disposable.dispose();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.error("onWebSocketBinary:: binary data processing is not implemented.");
    }

    @Override
    public void onWebSocketText(String message) {
        logger.info("onWebSocketText {}", message);
        GiraOneMessageType type = gson.fromJson(message, GiraOneMessageType.class);


    }

    @Override
    public void onWebSocketClose(int i, String s) {
        logger.info("onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.info("onWebSocketConnect");
        this.websocketSession = session;
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.info("onWebSocketError");
    }


    @Override public void registerApplication() {
        GiraOneCommandResponse resp = execute(RegisterApplication.builder().build());
    }
}