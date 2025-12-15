package com.hl7testbench.transport;

import com.hl7testbench.model.TransportResult;

/**
 * Simple callback interface for transport operation results.
 */
@FunctionalInterface
public interface TransportCallback {

    /**
     * Called when a transport operation completes.
     *
     * @param result the transport result
     */
    void onComplete(TransportResult result);
}
