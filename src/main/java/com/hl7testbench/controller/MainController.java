package com.hl7testbench.controller;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.HL7Message;
import com.hl7testbench.model.TransportResult;
import com.hl7testbench.observer.TransportObserver;
import com.hl7testbench.observer.TransportSubject;
import com.hl7testbench.transport.TransportFactory;
import com.hl7testbench.transport.TransportStrategy;
import com.hl7testbench.util.HL7Parser;
import com.hl7testbench.view.*;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * Main controller coordinating between views and transport layer.
 * Implements Observer pattern for receiving transport notifications and updating UI.
 */
public class MainController implements TransportObserver {

    private final MainFrame mainFrame;
    private final TransportSubject transportSubject;
    private TransportWorker currentWorker;

    public MainController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transportSubject = new TransportSubject();
        this.transportSubject.addObserver(this);

        initializeEventHandlers();
    }

    private void initializeEventHandlers() {
        MessagePanel messagePanel = mainFrame.getMessagePanel();

        messagePanel.getSendButton().addActionListener(e -> sendCurrentMessage());
        messagePanel.getSendAllButton().addActionListener(e -> sendAllMessages());
    }

    /**
     * Sends the message currently in the text area.
     */
    private void sendCurrentMessage() {
        String content = mainFrame.getMessagePanel().getMessageContent();

        if (content.isBlank()) {
            showWarning("No Message", "Please paste or load an HL7 message first.");
            return;
        }

        if (!HL7Parser.isValidHL7(content)) {
            showWarning("Invalid HL7",
                    "The content does not appear to be a valid HL7 message.\n" +
                    "Messages must contain an MSH segment.");
            return;
        }

        ConnectionConfig config = mainFrame.getConnectionPanel().getConnectionConfig();
        if (!validateConfig(config)) {
            return;
        }

        HL7Message message = HL7Parser.parseSingleMessage(content);
        if (message == null) {
            showWarning("Parse Error", "Failed to parse the HL7 message.");
            return;
        }

        sendMessages(Collections.singletonList(message));
    }

    /**
     * Sends all loaded messages.
     */
    private void sendAllMessages() {
        List<HL7Message> allMessages = mainFrame.getMessagePanel().getAllMessages();

        if (allMessages.isEmpty()) {
            showWarning("No Messages", "Please load messages from a file first.");
            return;
        }

        ConnectionConfig config = mainFrame.getConnectionPanel().getConnectionConfig();
        if (!validateConfig(config)) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Send all " + allMessages.size() + " message(s)?",
                "Confirm Send All",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            sendMessages(allMessages);
        }
    }

    /**
     * Validates the connection configuration.
     */
    private boolean validateConfig(ConnectionConfig config) {
        TransportStrategy transport = TransportFactory.getTransport(config);

        if (!transport.validateConfig(config)) {
            if (config.mode() == ConnectionConfig.TransportMode.MLLP_TCP) {
                showWarning("Invalid Configuration",
                        "Please enter a valid host and port for MLLP connection.");
            } else {
                showWarning("Invalid Configuration",
                        "Please enter a valid HTTP/HTTPS URL.");
            }
            return false;
        }
        return true;
    }

    /**
     * Initiates sending of the given messages using a background worker.
     */
    private void sendMessages(List<HL7Message> messages) {
        if (currentWorker != null && !currentWorker.isDone()) {
            showWarning("Busy", "A transport operation is already in progress.");
            return;
        }

        ConnectionConfig config = mainFrame.getConnectionPanel().getConnectionConfig();

        setUIBusy(true);
        mainFrame.getStatusBar().setBusy("Sending " + messages.size() + " message(s)...");

        currentWorker = new TransportWorker(messages, config, transportSubject);
        currentWorker.execute();
    }

    /**
     * Sets UI elements enabled/disabled based on busy state.
     */
    private void setUIBusy(boolean busy) {
        mainFrame.getMessagePanel().getSendButton().setEnabled(!busy);
        mainFrame.getMessagePanel().getSendAllButton().setEnabled(!busy);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void onTransportStarted(String messageControlId) {
        mainFrame.getStatusBar().setBusy("Sending: " + messageControlId);
    }

    @Override
    public void onTransportCompleted(TransportResult result) {
        mainFrame.getHistoryPanel().addResult(result);

        if (result.status().isSuccessful()) {
            mainFrame.getStatusBar().setIdle("Success: " + result.messageControlId());
        } else {
            mainFrame.getStatusBar().setError("Failed: " + result.messageControlId() +
                    " - " + result.status().getDisplayName());
        }

        setUIBusy(false);
    }

    @Override
    public void onTransportProgress(String message) {
        mainFrame.getStatusBar().setIdle(message);
        setUIBusy(false);
    }
}
