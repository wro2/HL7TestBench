package com.hl7testbench.view;

import com.hl7testbench.util.UIConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Application status bar displaying current operation status.
 */
public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    public StatusBar() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
                new BevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UIConstants.LABEL_FONT);
        statusLabel.setForeground(Color.DARK_GRAY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setVisible(false);

        add(statusLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.EAST);
    }

    public void setIdle(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.DARK_GRAY);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    public void setBusy(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

    public void setError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }
}
