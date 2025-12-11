package com.hl7testbench.view;

import com.hl7testbench.model.HL7Message;
import com.hl7testbench.util.HL7Parser;
import com.hl7testbench.util.UIConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified message panel combining message input, file loading, and editing.
 * Users paste or load messages here, then send directly.
 */
public class MessagePanel extends JPanel {

    private final JTextArea messageArea;
    private final JTable messageTable;
    private final DefaultTableModel tableModel;
    private final JButton loadFileButton;
    private final JButton sendButton;
    private final JButton sendAllButton;
    private final JButton clearButton;
    private final JLabel statusLabel;

    private final List<HL7Message> loadedMessages = new ArrayList<>();

    private static final String[] COLUMN_NAMES = {"#", "Type", "Control ID"};

    public MessagePanel() {
        setLayout(new BorderLayout(10, 10));
        TitledBorder border = new TitledBorder("HL7 Message");
        border.setTitleFont(UIConstants.TITLE_FONT);
        setBorder(border);

        messageArea = new JTextArea();
        messageArea.setFont(UIConstants.MONOSPACE_FONT);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(false);
        messageArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel messageAreaPanel = new JPanel(new BorderLayout());
        JLabel instructionLabel = new JLabel("Paste an HL7 message below, or load from file:");
        instructionLabel.setFont(UIConstants.LABEL_FONT);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        messageAreaPanel.add(instructionLabel, BorderLayout.NORTH);
        messageAreaPanel.add(messageScrollPane, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        messageTable = new JTable(tableModel);
        messageTable.setFont(UIConstants.TABLE_FONT);
        messageTable.getTableHeader().setFont(UIConstants.TABLE_HEADER_FONT);
        messageTable.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        messageTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        messageTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        messageTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        messageTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane tableScrollPane = new JScrollPane(messageTable);
        tableScrollPane.setPreferredSize(new Dimension(300, 120));

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        JLabel tableLabel = new JLabel("Loaded Messages (select to edit):");
        tableLabel.setFont(UIConstants.LABEL_FONT);
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("No messages loaded");
        statusLabel.setFont(UIConstants.LABEL_FONT);

        tablePanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        loadFileButton = createButton("Load File...");
        sendButton = createButton("Send Message");
        sendAllButton = createButton("Send All");
        clearButton = createButton("Clear");

        sendButton.setBackground(new Color(46, 125, 50));
        sendButton.setForeground(Color.WHITE);

        buttonPanel.add(loadFileButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(sendAllButton);
        buttonPanel.add(clearButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                messageAreaPanel, tablePanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(600);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadFileButton.addActionListener(e -> loadFromFile());
        clearButton.addActionListener(e -> clear());

        messageTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedToEditor();
            }
        });
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setPreferredSize(new Dimension(130, 35));
        return button;
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select HL7 Message File");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "HL7 Files (*.hl7, *.txt)", "hl7", "txt"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "All Files (*.*)", "*"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path filePath = chooser.getSelectedFile().toPath();
                List<HL7Message> parsed = HL7Parser.parseFile(filePath);

                if (parsed.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No valid HL7 messages found in the file.\n" +
                            "Messages must contain an MSH segment.",
                            "No Messages Found",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                setMessages(parsed);

                if (!parsed.isEmpty()) {
                    messageArea.setText(parsed.get(0).getRawContent());
                    messageTable.setRowSelectionInterval(0, 0);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + ex.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSelectedToEditor() {
        int selectedRow = messageTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < loadedMessages.size()) {
            HL7Message message = loadedMessages.get(selectedRow);
            messageArea.setText(message.getRawContent());
            messageArea.setCaretPosition(0);
        }
    }

    public void setMessages(List<HL7Message> messages) {
        loadedMessages.clear();
        loadedMessages.addAll(messages);
        refreshTable();
        updateStatus();
    }

    public void clear() {
        messageArea.setText("");
        loadedMessages.clear();
        tableModel.setRowCount(0);
        updateStatus();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        int index = 1;
        for (HL7Message msg : loadedMessages) {
            tableModel.addRow(new Object[]{
                    index++,
                    msg.getFullMessageType(),
                    msg.getMessageControlId()
            });
        }
    }

    private void updateStatus() {
        int count = loadedMessages.size();
        statusLabel.setText(count == 0 ?
                "No messages loaded" :
                count + " message(s) loaded");
    }

    public String getMessageContent() {
        return messageArea.getText();
    }

    public void setMessageContent(String content) {
        messageArea.setText(content);
        messageArea.setCaretPosition(0);
    }

    public List<HL7Message> getSelectedMessages() {
        List<HL7Message> selected = new ArrayList<>();
        int[] selectedRows = messageTable.getSelectedRows();
        for (int row : selectedRows) {
            if (row >= 0 && row < loadedMessages.size()) {
                selected.add(loadedMessages.get(row));
            }
        }
        return selected;
    }

    public List<HL7Message> getAllMessages() {
        return new ArrayList<>(loadedMessages);
    }

    public void addTableSelectionListener(ListSelectionListener listener) {
        messageTable.getSelectionModel().addListSelectionListener(listener);
    }

    public JButton getSendButton() {
        return sendButton;
    }

    public JButton getSendAllButton() {
        return sendAllButton;
    }

    public JButton getLoadFileButton() {
        return loadFileButton;
    }

    public JTextArea getMessageArea() {
        return messageArea;
    }
}
