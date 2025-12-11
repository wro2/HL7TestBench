package com.hl7testbench.controller;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.HL7Message;
import com.hl7testbench.model.TransportResult;
import com.hl7testbench.observer.TransportSubject;
import com.hl7testbench.transport.TransportFactory;
import com.hl7testbench.transport.TransportStrategy;

import javax.swing.*;
import java.util.List;

/**
 * Background worker for executing HL7 message transport operations.
 * Runs network operations off the EDT to prevent UI freezing.
 */
public class TransportWorker extends SwingWorker<Void, TransportResult> {

    private final List<HL7Message> messages;
    private final ConnectionConfig config;
    private final TransportSubject subject;

    /**
     * Creates a transport worker for sending multiple messages.
     *
     * @param messages the messages to send
     * @param config the connection configuration
     * @param subject the subject for notifying observers
     */
    public TransportWorker(List<HL7Message> messages, ConnectionConfig config,
                          TransportSubject subject) {
        this.messages = messages;
        this.config = config;
        this.subject = subject;
    }

    @Override
    protected Void doInBackground() {
        TransportStrategy transport = TransportFactory.getTransport(config);

        for (int i = 0; i < messages.size() && !isCancelled(); i++) {
            HL7Message message = messages.get(i);
            String controlId = message.getMessageControlId();

            SwingUtilities.invokeLater(() ->
                    subject.notifyTransportStarted(controlId));

            TransportResult result = transport.send(
                    message.getRawContent(),
                    controlId,
                    config
            );

            publish(result);

            if (i < messages.size() - 1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return null;
    }

    @Override
    protected void process(List<TransportResult> chunks) {
        for (TransportResult result : chunks) {
            subject.notifyTransportCompleted(result);
        }
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            subject.notifyProgress("Transport cancelled");
        } else {
            subject.notifyProgress("Transport complete");
        }
    }
}
