package com.hl7testbench.util;

import com.hl7testbench.model.HL7Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing HL7 messages from various sources.
 * Handles message splitting, normalization, and validation.
 */
public final class HL7Parser {

    private static final Pattern MSH_SEGMENT_PATTERN = Pattern.compile(
            "(?m)^MSH\\|",
            Pattern.MULTILINE
    );

    private HL7Parser() {
    }

    /**
     * Parses a file containing one or more HL7 messages.
     * Messages are split based on MSH segment occurrences.
     *
     * @param filePath the path to the file
     * @return a list of parsed HL7 messages
     * @throws IOException if the file cannot be read
     */
    public static List<HL7Message> parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return parseMultipleMessages(content);
    }

    /**
     * Parses a string containing one or more HL7 messages.
     * Messages are split based on MSH segment occurrences.
     *
     * @param content the raw content potentially containing multiple messages
     * @return a list of parsed HL7 messages
     */
    public static List<HL7Message> parseMultipleMessages(String content) {
        List<HL7Message> messages = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return messages;
        }

        String normalized = normalizeContent(content);

        Matcher matcher = MSH_SEGMENT_PATTERN.matcher(normalized);
        List<Integer> mshPositions = new ArrayList<>();

        while (matcher.find()) {
            mshPositions.add(matcher.start());
        }

        if (mshPositions.isEmpty()) {
            return messages;
        }

        for (int i = 0; i < mshPositions.size(); i++) {
            int start = mshPositions.get(i);
            int end = (i + 1 < mshPositions.size()) ? mshPositions.get(i + 1) : normalized.length();

            String messageContent = normalized.substring(start, end).trim();
            if (!messageContent.isEmpty()) {
                messages.add(new HL7Message(messageContent));
            }
        }

        return messages;
    }

    /**
     * Parses a single HL7 message from raw content.
     *
     * @param content the raw HL7 message content
     * @return the parsed HL7 message, or null if invalid
     */
    public static HL7Message parseSingleMessage(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String normalized = normalizeContent(content);
        if (!normalized.contains("MSH|")) {
            return null;
        }

        return new HL7Message(normalized);
    }

    /**
     * Validates whether the content appears to be a valid HL7 message.
     *
     * @param content the content to validate
     * @return true if the content appears to be a valid HL7 message
     */
    public static boolean isValidHL7(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        String normalized = normalizeContent(content);
        return normalized.contains("MSH|") &&
                normalized.split("\\r").length >= 1;
    }

    /**
     * Normalizes content by converting various line ending styles to CR.
     */
    private static String normalizeContent(String content) {
        return content.replace("\r\n", "\r")
                .replace("\n", "\r")
                .trim();
    }

    /**
     * Extracts the message control ID from a raw HL7 message.
     *
     * @param rawMessage the raw message content
     * @return the message control ID, or "UNKNOWN" if not found
     */
    public static String extractMessageControlId(String rawMessage) {
        if (rawMessage == null) {
            return "UNKNOWN";
        }

        String normalized = normalizeContent(rawMessage);
        String[] segments = normalized.split("\r");

        for (String segment : segments) {
            if (segment.startsWith("MSH|")) {
                String[] fields = segment.split("\\|", -1);
                if (fields.length > 9) {
                    return fields[9].isEmpty() ? "UNKNOWN" : fields[9];
                }
            }
        }

        return "UNKNOWN";
    }
}
