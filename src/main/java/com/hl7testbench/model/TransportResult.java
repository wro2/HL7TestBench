package com.hl7testbench.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable result of a message transport operation.
 * Contains response data, timing, and status information.
 */
public record TransportResult(
        LocalDateTime timestamp,
        String messageControlId,
        ConnectionConfig.TransportMode transportMode,
        TransportStatus status,
        String rawResponse,
        long roundTripTimeMs,
        String errorMessage
) {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Status classification for transport results.
     */
    public enum TransportStatus {
        ACK_AA("ACK (AA)", true),
        ACK_AE("ACK (AE)", false),
        ACK_AR("ACK (AR)", false),
        TIMEOUT("Timeout", false),
        CONNECTION_ERROR("Connection Error", false),
        UNKNOWN_RESPONSE("Unknown Response", false),
        SUCCESS("Success", true);

        private final String displayName;
        private final boolean successful;

        TransportStatus(String displayName, boolean successful) {
            this.displayName = displayName;
            this.successful = successful;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Creates a successful result from an ACK response.
     */
    public static TransportResult success(
            String messageControlId,
            ConnectionConfig.TransportMode mode,
            String rawResponse,
            long roundTripTimeMs
    ) {
        TransportStatus status = parseAckStatus(rawResponse);
        return new TransportResult(
                LocalDateTime.now(),
                messageControlId,
                mode,
                status,
                rawResponse,
                roundTripTimeMs,
                null
        );
    }

    /**
     * Creates an error result from an exception.
     */
    public static TransportResult error(
            String messageControlId,
            ConnectionConfig.TransportMode mode,
            String errorMessage,
            long elapsedTimeMs
    ) {
        TransportStatus status = errorMessage.toLowerCase().contains("timeout")
                ? TransportStatus.TIMEOUT
                : TransportStatus.CONNECTION_ERROR;

        return new TransportResult(
                LocalDateTime.now(),
                messageControlId,
                mode,
                status,
                "",
                elapsedTimeMs,
                errorMessage
        );
    }

    /**
     * Parses the MSA segment to determine ACK status.
     */
    private static TransportStatus parseAckStatus(String response) {
        if (response == null || response.isEmpty()) {
            return TransportStatus.UNKNOWN_RESPONSE;
        }

        String normalized = response.replace("\r\n", "\r").replace("\n", "\r");
        String[] segments = normalized.split("\r");

        for (String segment : segments) {
            if (segment.startsWith("MSA|") || segment.startsWith("MSA^")) {
                String[] fields = segment.split("[|^]", -1);
                if (fields.length > 1) {
                    String ackCode = fields[1].trim().toUpperCase();
                    return switch (ackCode) {
                        case "AA", "CA" -> TransportStatus.ACK_AA;
                        case "AE", "CE" -> TransportStatus.ACK_AE;
                        case "AR", "CR" -> TransportStatus.ACK_AR;
                        default -> TransportStatus.UNKNOWN_RESPONSE;
                    };
                }
            }
        }

        return TransportStatus.UNKNOWN_RESPONSE;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(TIMESTAMP_FORMAT);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    public String getDisplayResponse() {
        if (hasError()) {
            return "ERROR: " + errorMessage;
        }
        return rawResponse;
    }
}
