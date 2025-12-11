package com.hl7testbench.observer;

import com.hl7testbench.model.TransportResult;

/**
 * Observer interface for receiving transport operation notifications.
 * Implementations should handle UI updates on the appropriate thread.
 */
public interface TransportObserver {

    /**
     * Called when a message transport operation begins.
     *
     * @param messageControlId the control ID of the message being sent
     */
    void onTransportStarted(String messageControlId);

    /**
     * Called when a message transport operation completes.
     *
     * @param result the transport result containing response or error information
     */
    void onTransportCompleted(TransportResult result);

    /**
     * Called when progress information is available during transport.
     *
     * @param message a progress message
     */
    void onTransportProgress(String message);
}
