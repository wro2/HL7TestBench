package com.hl7testbench.transport;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.TransportResult;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

/**
 * MLLP (Minimal Lower Layer Protocol) transport implementation.
 * Wraps HL7 messages with standard MLLP framing characters:
 * - Start Block: VT (0x0B)
 * - End Block: FS CR (0x1C 0x0D)
 */
public class MllpTransport implements TransportStrategy {

    private static final byte START_BLOCK = 0x0B;
    private static final byte END_BLOCK = 0x1C;
    private static final byte CARRIAGE_RETURN = 0x0D;

    @Override
    public TransportResult send(String message, String messageControlId, ConnectionConfig config) {
        long startTime = System.currentTimeMillis();

        try (Socket socket = createSocket(config)) {
            socket.setSoTimeout(config.timeoutMs());
            socket.connect(new InetSocketAddress(config.host(), config.port()), config.timeoutMs());

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            byte[] framedMessage = frameMessage(message);
            out.write(framedMessage);
            out.flush();

            String response = readResponse(in, config.timeoutMs());
            long roundTripTime = System.currentTimeMillis() - startTime;

            return TransportResult.success(messageControlId, config.mode(), response, roundTripTime);

        } catch (SocketTimeoutException e) {
            return TransportResult.error(
                    messageControlId,
                    config.mode(),
                    "Connection timeout after " + config.timeoutMs() + "ms",
                    System.currentTimeMillis() - startTime
            );
        } catch (IOException e) {
            return TransportResult.error(
                    messageControlId,
                    config.mode(),
                    "Connection error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime
            );
        } catch (Exception e) {
            return TransportResult.error(
                    messageControlId,
                    config.mode(),
                    "Unexpected error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime
            );
        }
    }

    /**
     * Creates a socket with optional TLS/SSL support.
     */
    private Socket createSocket(ConnectionConfig config) throws Exception {
        if (config.useTls()) {
            SSLContext sslContext = createSslContext(config);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            return factory.createSocket();
        }
        return new Socket();
    }

    /**
     * Creates an SSL context using the configured keystore.
     */
    private SSLContext createSslContext(ConnectionConfig config) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        KeyManagerFactory kmf = null;
        TrustManagerFactory tmf = null;

        if (config.keystoreFile() != null && config.keystoreFile().exists()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(config.keystoreFile())) {
                keyStore.load(fis, config.keystorePassword());
            }

            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, config.keystorePassword());

            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } else {
            sslContext.init(null, null, null);
        }

        return sslContext;
    }

    /**
     * Frames an HL7 message with MLLP delimiters.
     */
    private byte[] frameMessage(String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] framed = new byte[messageBytes.length + 3];

        framed[0] = START_BLOCK;
        System.arraycopy(messageBytes, 0, framed, 1, messageBytes.length);
        framed[framed.length - 2] = END_BLOCK;
        framed[framed.length - 1] = CARRIAGE_RETURN;

        return framed;
    }

    /**
     * Reads an MLLP-framed response from the input stream.
     * Strips the framing characters before returning.
     */
    private String readResponse(InputStream in, int timeoutMs) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long deadline = System.currentTimeMillis() + timeoutMs;
        boolean startFound = false;
        boolean endBlockFound = false;

        while (System.currentTimeMillis() < deadline) {
            int available = in.available();
            if (available == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            int b = in.read();
            if (b == -1) break;

            if (b == START_BLOCK) {
                startFound = true;
                continue;
            }

            if (b == END_BLOCK) {
                endBlockFound = true;
                continue;
            }

            if (endBlockFound && b == CARRIAGE_RETURN) {
                break;
            }

            if (startFound) {
                buffer.write(b);
                endBlockFound = false;
            }
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return "MLLP (TCP)";
    }

    @Override
    public boolean validateConfig(ConnectionConfig config) {
        return config.host() != null &&
                !config.host().isBlank() &&
                config.port() > 0 &&
                config.port() <= 65535;
    }
}
