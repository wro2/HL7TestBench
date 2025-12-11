package com.hl7testbench.transport;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.TransportResult;

/**
 * Strategy interface for HL7 message transport.
 * Implementations provide specific transport mechanisms (MLLP, HTTP, etc.).
 */
public interface TransportStrategy {

    /**
     * Sends an HL7 message and returns the transport result.
     *
     * @param message the raw HL7 message content
     * @param messageControlId the message control ID for tracking
     * @param config the connection configuration
     * @return the transport result containing response or error information
     */
    TransportResult send(String message, String messageControlId, ConnectionConfig config);

    /**
     * Returns a human-readable name for this transport strategy.
     */
    String getName();

    /**
     * Validates the configuration for this transport type.
     *
     * @param config the configuration to validate
     * @return true if the configuration is valid for this transport
     */
    boolean validateConfig(ConnectionConfig config);
}
