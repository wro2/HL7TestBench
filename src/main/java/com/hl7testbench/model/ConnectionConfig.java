package com.hl7testbench.model;

import java.io.File;

/**
 * Immutable configuration for connection settings.
 * Supports both MLLP (TCP) and HTTP transport modes.
 */
public record ConnectionConfig(
        TransportMode mode,
        String host,
        int port,
        String httpUrl,
        boolean useTls,
        File keystoreFile,
        char[] keystorePassword,
        int timeoutMs
) {

    /**
     * Transport mode enumeration.
     */
    public enum TransportMode {
        MLLP_TCP("MLLP (TCP)"),
        HTTP("HTTP/HTTPS");

        private final String displayName;

        TransportMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Creates a default configuration for MLLP transport.
     */
    public static ConnectionConfig defaultMllp() {
        return new ConnectionConfig(
                TransportMode.MLLP_TCP,
                "localhost",
                2575,
                "",
                false,
                null,
                null,
                10000
        );
    }

    /**
     * Creates a default configuration for HTTP transport.
     */
    public static ConnectionConfig defaultHttp() {
        return new ConnectionConfig(
                TransportMode.HTTP,
                "",
                0,
                "http://localhost:8080/hl7",
                false,
                null,
                null,
                10000
        );
    }

    /**
     * Creates a new configuration with updated timeout.
     */
    public ConnectionConfig withTimeout(int newTimeoutMs) {
        return new ConnectionConfig(mode, host, port, httpUrl, useTls,
                keystoreFile, keystorePassword, newTimeoutMs);
    }
}
