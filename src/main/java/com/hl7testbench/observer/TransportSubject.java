package com.hl7testbench.observer;

import com.hl7testbench.model.TransportResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject class for the Observer pattern.
 * Manages transport observers and notifies them of transport events.
 */
public class TransportSubject {

    private final List<TransportObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Registers an observer to receive transport notifications.
     *
     * @param observer the observer to register
     */
    public void addObserver(TransportObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer from receiving transport notifications.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(TransportObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers that a transport operation has started.
     *
     * @param messageControlId the control ID of the message being sent
     */
    public void notifyTransportStarted(String messageControlId) {
        for (TransportObserver observer : observers) {
            observer.onTransportStarted(messageControlId);
        }
    }

    /**
     * Notifies all observers that a transport operation has completed.
     *
     * @param result the transport result
     */
    public void notifyTransportCompleted(TransportResult result) {
        for (TransportObserver observer : observers) {
            observer.onTransportCompleted(result);
        }
    }

    /**
     * Notifies all observers of transport progress.
     *
     * @param message the progress message
     */
    public void notifyProgress(String message) {
        for (TransportObserver observer : observers) {
            observer.onTransportProgress(message);
        }
    }
}
