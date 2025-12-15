package com.hl7testbench.transport;

import com.hl7testbench.model.ConnectionConfig;

/**
 * Factory for creating transport strategy instances.
 * Supports both default singleton instances and custom injection for testing.
 */
public class TransportFactory {

    private final TransportStrategy mllpTransport;
    private final TransportStrategy httpTransport;

    private static final TransportFactory DEFAULT_INSTANCE = new TransportFactory(
            new MllpTransport(),
            new HttpTransport()
    );

    /**
     * Creates a factory with custom transport implementations.
     * Useful for testing with mocks.
     */
    public TransportFactory(TransportStrategy mllpTransport, TransportStrategy httpTransport) {
        this.mllpTransport = mllpTransport;
        this.httpTransport = httpTransport;
    }

    /**
     * Returns the default factory instance.
     */
    public static TransportFactory getDefault() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Returns the appropriate transport strategy for the given configuration.
     *
     * @param config the connection configuration
     * @return the corresponding transport strategy
     */
    public TransportStrategy getTransport(ConnectionConfig config) {
        return switch (config.mode()) {
            case MLLP_TCP -> mllpTransport;
            case HTTP -> httpTransport;
        };
    }

    /**
     * Convenience static method using default factory.
     */
    public static TransportStrategy forConfig(ConnectionConfig config) {
        return DEFAULT_INSTANCE.getTransport(config);
    }
}
