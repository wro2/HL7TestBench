package com.hl7testbench.view;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.ConnectionConfig.TransportMode;
import com.hl7testbench.model.SavedServer;
import com.hl7testbench.util.UIConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified panel for configuring connection settings with saved server support.
 */
public class ConnectionPanel extends JPanel {

    private final JComboBox<SavedServer> serverComboBox;
    private final JComboBox<TransportMode> modeComboBox;
    private final JTextField hostField;
    private final JSpinner portSpinner;
    private final JTextField urlField;
    private final JCheckBox tlsCheckBox;
    private final JTextField keystorePathField;
    private final JPasswordField keystorePasswordField;
    private final JSpinner timeoutSpinner;

    private final JPanel mllpFieldsPanel;
    private final JPanel httpFieldsPanel;

    private final JButton saveServerButton;
    private final JButton deleteServerButton;

    private List<SavedServer> savedServers;
    private boolean updatingFromServer = false;

    public ConnectionPanel() {
        setLayout(new BorderLayout(10, 10));
        TitledBorder border = new TitledBorder("Server Connection");
        border.setTitleFont(UIConstants.TITLE_FONT);
        setBorder(border);

        savedServers = new ArrayList<>(SavedServer.loadAll());

        serverComboBox = new JComboBox<>();
        serverComboBox.setFont(UIConstants.INPUT_FONT);
        refreshServerComboBox();

        modeComboBox = new JComboBox<>(TransportMode.values());
        modeComboBox.setFont(UIConstants.INPUT_FONT);

        hostField = createTextField("localhost", 15);
        portSpinner = createSpinner(2575, 1, 65535);
        urlField = createTextField("http://localhost:8080/hl7", 30);
        tlsCheckBox = new JCheckBox("Use TLS/SSL");
        tlsCheckBox.setFont(UIConstants.LABEL_FONT);
        keystorePathField = createTextField("", 20);
        keystorePathField.setEditable(false);
        keystorePasswordField = new JPasswordField(15);
        keystorePasswordField.setFont(UIConstants.INPUT_FONT);
        timeoutSpinner = createSpinner(10000, 1000, 300000);

        saveServerButton = createButton("Save Server");
        deleteServerButton = createButton("Delete");

        mllpFieldsPanel = createMllpFieldsPanel();
        httpFieldsPanel = createHttpFieldsPanel();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(createLabel("Saved Servers:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        mainPanel.add(serverComboBox, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        mainPanel.add(saveServerButton, gbc);
        gbc.gridx = 4;
        mainPanel.add(deleteServerButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(createLabel("Mode:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(modeComboBox, gbc);
        gbc.gridx = 2;
        mainPanel.add(createLabel("Timeout (ms):"), gbc);
        gbc.gridx = 3;
        mainPanel.add(timeoutSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel connectionFieldsPanel = new JPanel(new CardLayout());
        connectionFieldsPanel.add(mllpFieldsPanel, TransportMode.MLLP_TCP.name());
        connectionFieldsPanel.add(httpFieldsPanel, TransportMode.HTTP.name());
        mainPanel.add(connectionFieldsPanel, gbc);

        gbc.gridy = 3; gbc.gridwidth = 5;
        mainPanel.add(createTlsPanel(), gbc);

        add(mainPanel, BorderLayout.CENTER);

        modeComboBox.addActionListener(e -> {
            if (!updatingFromServer) {
                CardLayout cl = (CardLayout) connectionFieldsPanel.getLayout();
                TransportMode mode = (TransportMode) modeComboBox.getSelectedItem();
                if (mode != null) {
                    cl.show(connectionFieldsPanel, mode.name());
                }
            }
        });

        serverComboBox.addActionListener(e -> loadSelectedServer());
        saveServerButton.addActionListener(e -> saveCurrentServer());
        deleteServerButton.addActionListener(e -> deleteSelectedServer());
        tlsCheckBox.addActionListener(e -> updateTlsFieldsEnabled());

        updateTlsFieldsEnabled();

        if (!savedServers.isEmpty()) {
            serverComboBox.setSelectedIndex(0);
        }
    }

    private JPanel createMllpFieldsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(createLabel("Host/IP:"));
        panel.add(hostField);
        panel.add(createLabel("Port:"));
        panel.add(portSpinner);
        return panel;
    }

    private JPanel createHttpFieldsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(createLabel("URL:"));
        panel.add(urlField);
        return panel;
    }

    private JPanel createTlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(tlsCheckBox);
        panel.add(createLabel("KeyStore:"));
        panel.add(keystorePathField);

        JButton browseButton = createButton("Browse...");
        browseButton.addActionListener(e -> browseKeystore());
        panel.add(browseButton);

        panel.add(createLabel("Password:"));
        panel.add(keystorePasswordField);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.LABEL_FONT);
        return label;
    }

    private JTextField createTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        field.setFont(UIConstants.INPUT_FONT);
        return field;
    }

    private JSpinner createSpinner(int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.setFont(UIConstants.INPUT_FONT);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(6);
        return spinner;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        return button;
    }

    private void updateTlsFieldsEnabled() {
        boolean enabled = tlsCheckBox.isSelected();
        keystorePathField.setEnabled(enabled);
        keystorePasswordField.setEnabled(enabled);
    }

    private void browseKeystore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select KeyStore File");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "KeyStore Files (*.jks, *.p12, *.pfx)", "jks", "p12", "pfx"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            keystorePathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void refreshServerComboBox() {
        serverComboBox.removeAllItems();
        for (SavedServer server : savedServers) {
            serverComboBox.addItem(server);
        }
    }

    private void loadSelectedServer() {
        SavedServer selected = (SavedServer) serverComboBox.getSelectedItem();
        if (selected == null) return;

        updatingFromServer = true;
        try {
            modeComboBox.setSelectedItem(selected.mode());
            hostField.setText(selected.host());
            portSpinner.setValue(selected.port());
            urlField.setText(selected.httpUrl());
            tlsCheckBox.setSelected(selected.useTls());
            timeoutSpinner.setValue(selected.timeoutMs());

            Container parent = modeComboBox.getParent();
            while (parent != null) {
                for (Component c : parent.getComponents()) {
                    if (c instanceof JPanel panel && panel.getLayout() instanceof CardLayout cl) {
                        cl.show(panel, selected.mode().name());
                        break;
                    }
                }
                parent = parent.getParent();
            }
        } finally {
            updatingFromServer = false;
        }
        updateTlsFieldsEnabled();
    }

    private void saveCurrentServer() {
        String name = JOptionPane.showInputDialog(this,
                "Enter a name for this server configuration:",
                "Save Server",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.isBlank()) return;

        ConnectionConfig config = getConnectionConfig();
        SavedServer newServer = SavedServer.fromConfig(name.trim(), config);

        savedServers.removeIf(s -> s.name().equals(newServer.name()));
        savedServers.add(0, newServer);

        SavedServer.saveAll(savedServers);
        refreshServerComboBox();
        serverComboBox.setSelectedItem(newServer);

        JOptionPane.showMessageDialog(this,
                "Server configuration saved.",
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedServer() {
        SavedServer selected = (SavedServer) serverComboBox.getSelectedItem();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete server '" + selected.name() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            savedServers.remove(selected);
            SavedServer.saveAll(savedServers);
            refreshServerComboBox();
        }
    }

    public ConnectionConfig getConnectionConfig() {
        TransportMode mode = (TransportMode) modeComboBox.getSelectedItem();
        File keystoreFile = keystorePathField.getText().isEmpty() ?
                null : new File(keystorePathField.getText());

        return new ConnectionConfig(
                mode,
                hostField.getText().trim(),
                (Integer) portSpinner.getValue(),
                urlField.getText().trim(),
                tlsCheckBox.isSelected(),
                keystoreFile,
                keystorePasswordField.getPassword(),
                (Integer) timeoutSpinner.getValue()
        );
    }

    public TransportMode getSelectedMode() {
        return (TransportMode) modeComboBox.getSelectedItem();
    }
}
