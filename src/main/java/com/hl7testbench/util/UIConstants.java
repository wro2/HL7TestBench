package com.hl7testbench.util;

import java.awt.*;

/**
 * Centralized UI constants for consistent styling across the application.
 */
public final class UIConstants {

    private UIConstants() {
    }

    public static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font LABEL_FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font INPUT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    public static final Font TABLE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font TABLE_HEADER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public static final Color SUCCESS_COLOR = new Color(0, 128, 0);
    public static final Color ERROR_COLOR = new Color(178, 34, 34);
    public static final Color WARNING_COLOR = new Color(184, 134, 11);

    public static final int TABLE_ROW_HEIGHT = 28;
    public static final int FIELD_HEIGHT = 30;

    public static final Insets PANEL_INSETS = new Insets(10, 10, 10, 10);
}
