package org.openhab.binding.giraone.internal.communication;

public class GiraOneCommunicationException extends Exception {

    public GiraOneCommunicationException(String message) {
        super(message);
    }

    public GiraOneCommunicationException(String message, Throwable t) {
        super(message, t);
    }
}
