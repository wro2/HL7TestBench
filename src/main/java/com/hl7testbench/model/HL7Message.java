package com.hl7testbench.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed HL7 v2 message with extracted metadata.
 * Provides methods for extracting key fields from MSH segment.
 */
public class HL7Message {

    private static final Pattern MSH_PATTERN = Pattern.compile("^MSH\\|([^|]*)\\|", Pattern.MULTILINE);
    private static final char DEFAULT_FIELD_SEPARATOR = '|';

    private final String rawContent;
    private final String messageControlId;
    private final String messageType;
    private final String triggerEvent;
    private final String sendingApplication;
    private final String sendingFacility;

    /**
     * Constructs an HL7Message by parsing raw HL7 content.
     *
     * @param rawContent the raw HL7 message string
     */
    public HL7Message(String rawContent) {
        this.rawContent = normalizeLineEndings(rawContent);
        this.messageControlId = extractField(9);
        String[] msgTypeParts = extractField(8).split("\\^");
        this.messageType = msgTypeParts.length > 0 ? msgTypeParts[0] : "UNKNOWN";
        this.triggerEvent = msgTypeParts.length > 1 ? msgTypeParts[1] : "";
        this.sendingApplication = extractField(2);
        this.sendingFacility = extractField(3);
    }

    /**
     * Normalizes line endings to carriage return (HL7 standard segment terminator).
     */
    private String normalizeLineEndings(String content) {
        if (content == null) return "";
        return content.replace("\r\n", "\r").replace("\n", "\r");
    }

    /**
     * Extracts a field from the MSH segment by 1-based index.
     * Note: MSH-1 is the field separator itself, MSH-2 starts the actual fields.
     */
    private String extractField(int fieldIndex) {
        try {
            String[] segments = rawContent.split("\r");
            for (String segment : segments) {
                if (segment.startsWith("MSH")) {
                    String[] fields = segment.split("\\|", -1);
                    if (fieldIndex < fields.length) {
                        return fields[fieldIndex];
                    }
                }
            }
        } catch (Exception e) {
            // Return empty on parse failure
        }
        return "";
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getMessageControlId() {
        return messageControlId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public String getFullMessageType() {
        if (triggerEvent.isEmpty()) {
            return messageType;
        }
        return messageType + "^" + triggerEvent;
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public String getSendingFacility() {
        return sendingFacility;
    }

    /**
     * Returns a display-friendly summary of this message.
     */
    public String getDisplaySummary() {
        return String.format("%s - %s", getFullMessageType(), messageControlId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HL7Message that = (HL7Message) o;
        return Objects.equals(rawContent, that.rawContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawContent);
    }

    @Override
    public String toString() {
        return getDisplaySummary();
    }
}
