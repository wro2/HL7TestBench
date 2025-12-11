package com.hl7testbench.transport;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.TransportResult;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.KeyStore;
import java.time.Duration;

/**
 * HTTP/HTTPS transport implementation for HL7 messages.
 * Sends messages as POST requests with configurable content type.
 */
public class HttpTransport implements TransportStrategy {

    private static final String DEFAULT_CONTENT_TYPE = "application/hl7-v2";

    private String contentType = DEFAULT_CONTENT_TYPE;

    @Override
    public TransportResult send(String message, String messageControlId, ConnectionConfig config) {
        long startTime = System.currentTimeMillis();

        try {
            HttpClient client = buildHttpClient(config);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.httpUrl()))
                    .timeout(Duration.ofMillis(config.timeoutMs()))
                    .header("Content-Type", contentType)
                    .header("Accept", contentType)
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long roundTripTime = System.currentTimeMillis() - startTime;

            String responseBody = response.body();

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return TransportResult.success(messageControlId, config.mode(), responseBody, roundTripTime);
            } else {
                return TransportResult.error(
                        messageControlId,
                        config.mode(),
                        "HTTP " + response.statusCode() + ": " + responseBody,
                        roundTripTime
                );
            }

        } catch (HttpTimeoutException e) {
            return TransportResult.error(
                    messageControlId,
                    config.mode(),
                    "HTTP timeout after " + config.timeoutMs() + "ms",
                    System.currentTimeMillis() - startTime
            );
        } catch (Exception e) {
            return TransportResult.error(
                    messageControlId,
                    config.mode(),
                    "HTTP error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime
            );
        }
    }

    /**
     * Builds an HttpClient with optional TLS configuration.
     */
    private HttpClient buildHttpClient(ConnectionConfig config) throws Exception {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs()));

        if (config.useTls() && config.keystoreFile() != null && config.keystoreFile().exists()) {
            SSLContext sslContext = createSslContext(config);
            builder.sslContext(sslContext);
        }

        return builder.build();
    }

    /**
     * Creates an SSL context from the configured keystore.
     */
    private SSLContext createSslContext(ConnectionConfig config) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(config.keystoreFile())) {
            keyStore.load(fis, config.keystorePassword());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, config.keystorePassword());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    @Override
    public String getName() {
        return "HTTP/HTTPS";
    }

    @Override
    public boolean validateConfig(ConnectionConfig config) {
        if (config.httpUrl() == null || config.httpUrl().isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(config.httpUrl());
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType != null && !contentType.isBlank() ? contentType : DEFAULT_CONTENT_TYPE;
    }

    public String getContentType() {
        return contentType;
    }
}
