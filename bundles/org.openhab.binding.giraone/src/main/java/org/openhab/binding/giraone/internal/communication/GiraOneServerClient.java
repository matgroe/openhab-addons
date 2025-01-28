package org.openhab.binding.giraone.internal.communication;

/**
 *
 */
public interface GiraOneServerClient {

    /**
     * Connects to
     */
    public abstract void connect();

    /**
     * Terminates existing connection to GiraOneServerClient.
     */
    public abstract void disconnect();

    /**
     * Register
     */
    public void registerApplication();


}
