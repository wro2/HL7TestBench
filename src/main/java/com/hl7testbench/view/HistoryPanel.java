package com.hl7testbench.view;

import com.hl7testbench.model.TransportResult;
import com.hl7testbench.model.TransportResult.TransportStatus;
import com.hl7testbench.util.UIConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel displaying transport history with results table and response details.
 */
public class HistoryPanel extends JPanel {

    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private final JTextArea detailArea;
    private final JButton clearHistoryButton;
    private final JLabel countLabel;

    private final List<TransportResult> results = new ArrayList<>();

    private static final String[] COLUMN_NAMES = {
            "Time", "Control ID", "Transport", "Status", "RTT (ms)"
    };

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        TitledBorder border = new TitledBorder("Send History");
        border.setTitleFont(UIConstants.TITLE_FONT);
        setBorder(border);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(UIConstants.TABLE_FONT);
        historyTable.getTableHeader().setFont(UIConstants.TABLE_HEADER_FONT);
        historyTable.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        historyTable.setDefaultRenderer(Object.class, new StatusCellRenderer());

        JScrollPane tableScrollPane = new JScrollPane(historyTable);

        detailArea = new JTextArea(10, 40);
        detailArea.setFont(UIConstants.MONOSPACE_FONT);
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(false);
        detailArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane detailScrollPane = new JScrollPane(detailArea);
        JPanel detailPanel = new JPanel(new BorderLayout());
        JLabel detailLabel = new JLabel("Server Response:");
        detailLabel.setFont(UIConstants.LABEL_FONT);
        detailPanel.add(detailLabel, BorderLayout.NORTH);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(180);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        countLabel = new JLabel("0 entries");
        countLabel.setFont(UIConstants.LABEL_FONT);
        countLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.setFont(UIConstants.BUTTON_FONT);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearHistoryButton);

        bottomPanel.add(countLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedDetail();
            }
        });

        clearHistoryButton.addActionListener(e -> clearHistory());
    }

    public void addResult(TransportResult result) {
        results.add(0, result);

        tableModel.insertRow(0, new Object[]{
                result.getFormattedTimestamp(),
                result.messageControlId(),
                result.transportMode().getDisplayName(),
                result.status().getDisplayName(),
                result.roundTripTimeMs()
        });

        updateCountLabel();

        if (historyTable.getRowCount() > 0) {
            historyTable.setRowSelectionInterval(0, 0);
        }
    }

    public void clearHistory() {
        results.clear();
        tableModel.setRowCount(0);
        detailArea.setText("");
        updateCountLabel();
    }

    private void showSelectedDetail() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < results.size()) {
            TransportResult result = results.get(selectedRow);
            detailArea.setText(result.getDisplayResponse());
            detailArea.setCaretPosition(0);
        } else {
            detailArea.setText("");
        }
    }

    private void updateCountLabel() {
        int count = results.size();
        countLabel.setText(count + (count == 1 ? " entry" : " entries"));
    }

    public JButton getClearHistoryButton() {
        return clearHistoryButton;
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            c.setFont(UIConstants.TABLE_FONT);

            if (!isSelected && row < results.size()) {
                TransportResult result = results.get(row);
                TransportStatus status = result.status();

                if (status.isSuccessful()) {
                    c.setForeground(UIConstants.SUCCESS_COLOR);
                } else if (status == TransportStatus.TIMEOUT ||
                           status == TransportStatus.CONNECTION_ERROR) {
                    c.setForeground(UIConstants.ERROR_COLOR);
                } else if (status == TransportStatus.ACK_AE ||
                           status == TransportStatus.ACK_AR) {
                    c.setForeground(UIConstants.WARNING_COLOR);
                } else {
                    c.setForeground(table.getForeground());
                }
            } else {
                c.setForeground(table.getForeground());
            }

            return c;
        }
    }
}
