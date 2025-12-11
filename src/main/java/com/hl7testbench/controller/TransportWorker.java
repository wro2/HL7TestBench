package com.hl7testbench.controller;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.HL7Message;
import com.hl7testbench.model.TransportResult;
import com.hl7testbench.transport.TransportCallback;
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
    private final TransportStrategy transport;
    private final TransportCallback callback;
    private final Runnable onComplete;

    /**
     * Creates a transport worker for sending multiple messages.
     *
     * @param messages the messages to send
     * @param config the connection configuration
     * @param transport the transport strategy to use
     * @param callback callback for each message result
     * @param onComplete callback when all messages are processed
     */
    public TransportWorker(List<HL7Message> messages,
                           ConnectionConfig config,
                           TransportStrategy transport,
                           TransportCallback callback,
                           Runnable onComplete) {
        this.messages = messages;
        this.config = config;
        this.transport = transport;
        this.callback = callback;
        this.onComplete = onComplete;
    }

    @Override
    protected Void doInBackground() {
        for (int i = 0; i < messages.size() && !isCancelled(); i++) {
            HL7Message message = messages.get(i);

            TransportResult result = transport.send(
                    message.getRawContent(),
                    message.getMessageControlId(),
                    config
            );

            publish(result);

            if (i < messages.size() - 1) {
                try {
                    Thread.sleep(50);
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
            callback.onComplete(result);
        }
    }

    @Override
    protected void done() {
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
