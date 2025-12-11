package com.hl7testbench;

import com.hl7testbench.controller.MainController;
import com.hl7testbench.view.MainFrame;

import javax.swing.*;

/**
 * HL7 Test Bench - A Healthcare IT testing tool for sending HL7 v2 messages.
 *
 * <p>This application provides a graphical interface for sending HL7 messages
 * to server endpoints using either MLLP (TCP) or HTTP/HTTPS transport protocols.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Dual transport mode: MLLP (TCP) and HTTP/HTTPS</li>
 *   <li>TLS/SSL support with custom keystore configuration</li>
 *   <li>Manual message entry or batch file loading</li>
 *   <li>Message editing before transmission</li>
 *   <li>Transport history with ACK/NAK visualization</li>
 *   <li>Configurable timeouts</li>
 * </ul>
 *
 * @author HL7 Test Bench Development Team
 * @version 1.0.0
 */
public class HL7TestBench {

    /**
     * Application entry point.
     * Initializes the GUI on the Event Dispatch Thread.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        configureSystemProperties();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }

            MainFrame mainFrame = new MainFrame();
            MainController controller = new MainController(mainFrame);
            mainFrame.setVisible(true);
        });
    }

    /**
     * Configures system properties for better font rendering and UI behavior.
     */
    private static void configureSystemProperties() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        System.setProperty("sun.java2d.opengl", "false");
    }
}
