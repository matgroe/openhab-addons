package org.openhab.binding.giraone.internal.communication;

import org.openhab.binding.giraone.internal.communication.ws.GiraOneEvent;
import org.openhab.binding.giraone.internal.model.GiraOneProject;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 *
 */
public interface GiraOneServerClient {

    public enum ConnectionState {
        Disconnected,
        Connecting,
        Connected,
        Error
    }

    /**
     * Connects to
     */
    void connect() throws GiraOneCommunicationException;

    /**
     * Terminates existing connection to GiraOneServerClient.
     */
    void disconnect();

    /**
     * Provides the current
     * 
     * @return the {@link GiraOneProject}
     */
    GiraOneProject getProjectConfiguration();

    Disposable subscribeOnConnectionState(Consumer<ConnectionState> onNextEvent);

    Disposable subscribeOnGiraOneEvents(Consumer<GiraOneEvent> onNextEvent);
}
