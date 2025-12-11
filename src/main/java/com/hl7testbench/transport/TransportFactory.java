package com.hl7testbench.transport;

import com.hl7testbench.model.ConnectionConfig;

/**
 * Factory for creating transport strategy instances based on connection configuration.
 */
public final class TransportFactory {

    private static final MllpTransport MLLP_TRANSPORT = new MllpTransport();
    private static final HttpTransport HTTP_TRANSPORT = new HttpTransport();

    private TransportFactory() {
    }

    /**
     * Returns the appropriate transport strategy for the given configuration.
     *
     * @param config the connection configuration
     * @return the corresponding transport strategy
     * @throws IllegalArgumentException if the transport mode is not supported
     */
    public static TransportStrategy getTransport(ConnectionConfig config) {
        return switch (config.mode()) {
            case MLLP_TCP -> MLLP_TRANSPORT;
            case HTTP -> HTTP_TRANSPORT;
        };
    }

    /**
     * Returns the HTTP transport instance for configuration.
     */
    public static HttpTransport getHttpTransport() {
        return HTTP_TRANSPORT;
    }
}
