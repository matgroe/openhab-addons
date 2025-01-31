package org.openhab.binding.giraone.internal.communication.ws;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.giraone.internal.communication.GiraOneCommunicationException;
import org.openhab.binding.giraone.internal.communication.GiraOneServerClient;
import org.openhab.binding.giraone.internal.communication.ws.commands.GetUIConfiguration;
import org.openhab.binding.giraone.internal.communication.ws.commands.RegisterApplication;
import org.openhab.binding.giraone.internal.communication.ws.commands.ServerCommand;
import org.openhab.binding.giraone.internal.communication.ws.gson.GsonMapperFactory;
import org.openhab.binding.giraone.internal.model.GiraOneProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;

public class GiraOneWebsocketClient implements GiraOneServerClient, WebSocketListener {
    private final Logger logger = LoggerFactory.getLogger(GiraOneWebsocketClient.class);

    private final static String TEMPLATE_WEBSOCKET_URL = "wss://%s:4432/gds/api?%s";
    private final static int MAX_TEXT_MESSAGE_SIZE = (100 * 1024);
    private final static int TIMEOUT_SECONDS = 10;
    private final Subject<GiraOneCommandResponse> responses = PublishSubject.create();
    private final Subject<GiraOneEvent> events = PublishSubject.create();
    private final ReplaySubject<ConnectionState> connectionState = ReplaySubject.createWithSize(1);
    private final String giraOneWssEndpoint;
    private final Gson gson;

    private Session websocketSession = null;
    private QueuedThreadPool jettyThreadPool = null;

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
        this.giraOneWssEndpoint = String.format(TEMPLATE_WEBSOCKET_URL, host,
                computeWebsocketAuthToken(username, password));
        this.connectionState.onNext(ConnectionState.Disconnected);
    }

    String computeWebsocketAuthToken(String username, String password) {
        String auth = String.format("%s:%s", username, password);
        return "ui" + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
    }

    WebSocketClient createWebSocketClient(HttpClient httpClient) throws GiraOneCommunicationException {
        jettyThreadPool = new QueuedThreadPool();
        jettyThreadPool.setName(GiraOneServerClient.class.getSimpleName());
        jettyThreadPool.setDaemon(true);
        jettyThreadPool.setStopTimeout(0);

        try {
            jettyThreadPool.start();
        } catch (Exception e) {
            jettyThreadPool = null;
            throw new GiraOneCommunicationException("Cannot start new ThreadPool.", e);
        }
        WebSocketClient webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.setExecutor(jettyThreadPool);
        webSocketClient.getPolicy().setMaxTextMessageSize(MAX_TEXT_MESSAGE_SIZE);

        try {
            webSocketClient.start();
            return webSocketClient;
        } catch (Exception e) {
            throw new GiraOneCommunicationException("Cannot start new WebSocketClient.", e);
        }
    }

    void initiateWebsocketSession(WebSocketClient webSocketClient) throws GiraOneCommunicationException {
        try {
            Future<Session> clientSessionPromise = webSocketClient.connect(this, URI.create(giraOneWssEndpoint));
            clientSessionPromise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (IOException exp) {
            throw new GiraOneCommunicationException("Cannot initiate websocket session with " + giraOneWssEndpoint,
                    exp);
        } catch (InterruptedException | TimeoutException | ExecutionException exp) {
            throw new GiraOneCommunicationException("Cannot resolve client session with given timeout.", exp);
        }
    }

    @Override
    public void connect() throws GiraOneCommunicationException {
        logger.info("Connecting to {}", this.giraOneWssEndpoint);
        this.connectionState.onNext(ConnectionState.Connecting);
        HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
        WebSocketClient webSocketClient = createWebSocketClient(httpClient);
        initiateWebsocketSession(webSocketClient);
    }

    @Override
    public void disconnect() {
        try {
            if (this.websocketSession != null) {
                this.websocketSession.close();
            }
            if (this.jettyThreadPool != null) {
                this.jettyThreadPool.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.websocketSession = null;
            this.jettyThreadPool = null;
            this.connectionState.onNext(ConnectionState.Disconnected);
        }
    }

    @Override
    public GiraOneProject getProjectConfiguration() {
        if (connectionState.getValue() == ConnectionState.Connected) {
            return execute(GetUIConfiguration.builder().build()).getReply(GiraOneProject.class);
        }
        throw new IllegalStateException("Must be in ConnectionState.Connected");
    }

    @Override
    public @NonNull Disposable subscribeOnConnectionState(@NonNull Consumer<ConnectionState> onNext) {
        return this.connectionState.subscribe(onNext);
    }

    @Override
    public @NonNull Disposable subscribeOnGiraOneEvents(@NonNull Consumer<GiraOneEvent> onNext) {
        return this.events.subscribe(onNext);
    }

    /**
     * Sends a {@link ServerCommand} to Gira One Server. This method
     * is getting used as "fire and forget".
     *
     * @param command The command to send
     */
    void send(ServerCommand command) {
        try {
            String message = gson.toJson(command, ServerCommand.class);
            logger.info("ServerCommand '{}' :: {}", command.getCommand(), message);
            websocketSession.getRemote().sendString(message);
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Sends a {@link ServerCommand} to Gira One Server and waits
     * for the server's command response.
     *
     * @param command The command to send
     */
    GiraOneCommandResponse execute(ServerCommand command) {
        final CompletableFuture<GiraOneCommandResponse> promise = new CompletableFuture<>();
        Disposable disposable = Disposable.empty();
        try {
            disposable = this.responses.filter(f -> f.isInitatedBy(command)).take(1).subscribe(promise::complete);
            // send out command
            send(command);
            // and wait for response
            return promise.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException exp) {
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
        logger.info("Received Message :: {}", message);
        GiraOneMessageType type = gson.fromJson(message, GiraOneMessageType.class);
        switch (type) {
            case Event -> this.events.onNext(gson.fromJson(message, GiraOneEvent.class));
            case Response -> this.responses.onNext(gson.fromJson(message, GiraOneCommandResponse.class));
        }
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.info("WebSocket is closed with code={} and reason={}", code, reason);
        this.connectionState.onNext(ConnectionState.Disconnected);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("Received WebSocket Session :: {}", session);
        this.websocketSession = session;
        this.registerApplication();
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error("Received WebSocketError :: ", throwable);
        this.connectionState.onNext(ConnectionState.Error);
    }

    private void registerApplication() {
        CompletableFuture.runAsync(() -> {
            execute(RegisterApplication.builder().build());
            this.connectionState.onNext(ConnectionState.Connected);
        });
    }

    private void accept(GiraOneEvent e) {
        logger.info("Got Event {}", e);
    }
}
