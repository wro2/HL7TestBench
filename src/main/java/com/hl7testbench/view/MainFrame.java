package com.hl7testbench.view;

import com.hl7testbench.util.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Simplified main application window with cleaner layout.
 * Layout: Connection settings at top, Message area in center, History at bottom.
 */
public class MainFrame extends JFrame {

    private final ConnectionPanel connectionPanel;
    private final MessagePanel messagePanel;
    private final HistoryPanel historyPanel;
    private final StatusBar statusBar;

    private static final String APP_TITLE = "HL7 Test Bench";
    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 900;

    public MainFrame() {
        super(APP_TITLE);

        connectionPanel = new ConnectionPanel();
        messagePanel = new MessagePanel();
        historyPanel = new HistoryPanel();
        statusBar = new StatusBar();

        initializeLayout();
        configureFrame();
    }

    private void initializeLayout() {
        setLayout(new BorderLayout(0, 0));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(connectionPanel, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                messagePanel,
                historyPanel
        );
        mainSplit.setResizeWeight(0.45);
        mainSplit.setDividerLocation(320);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contentPanel.add(topSection, BorderLayout.NORTH);
        contentPanel.add(mainSplit, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setDefaultFont();
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Fall back to default look and feel
        }
    }

    private void setDefaultFont() {
        UIManager.put("Label.font", UIConstants.LABEL_FONT);
        UIManager.put("Button.font", UIConstants.BUTTON_FONT);
        UIManager.put("TextField.font", UIConstants.INPUT_FONT);
        UIManager.put("TextArea.font", UIConstants.MONOSPACE_FONT);
        UIManager.put("ComboBox.font", UIConstants.INPUT_FONT);
        UIManager.put("Table.font", UIConstants.TABLE_FONT);
        UIManager.put("TableHeader.font", UIConstants.TABLE_HEADER_FONT);
        UIManager.put("TitledBorder.font", UIConstants.TITLE_FONT);
    }

    public ConnectionPanel getConnectionPanel() {
        return connectionPanel;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public HistoryPanel getHistoryPanel() {
        return historyPanel;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }
}
