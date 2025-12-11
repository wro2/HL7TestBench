package com.hl7testbench.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Represents a saved server configuration that can be persisted and loaded.
 */
public record SavedServer(
        String name,
        ConnectionConfig.TransportMode mode,
        String host,
        int port,
        String httpUrl,
        boolean useTls,
        int timeoutMs
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hl7testbench";
    private static final String CONFIG_FILE = "servers.cfg";

    /**
     * Creates a SavedServer from a ConnectionConfig with a given name.
     */
    public static SavedServer fromConfig(String name, ConnectionConfig config) {
        return new SavedServer(
                name,
                config.mode(),
                config.host(),
                config.port(),
                config.httpUrl(),
                config.useTls(),
                config.timeoutMs()
        );
    }

    /**
     * Converts this SavedServer to a ConnectionConfig.
     */
    public ConnectionConfig toConnectionConfig() {
        return new ConnectionConfig(
                mode,
                host,
                port,
                httpUrl,
                useTls,
                null,
                null,
                timeoutMs
        );
    }

    /**
     * Loads all saved servers from the configuration file.
     */
    @SuppressWarnings("unchecked")
    public static List<SavedServer> loadAll() {
        Path configPath = Path.of(CONFIG_DIR, CONFIG_FILE);

        if (!Files.exists(configPath)) {
            return new ArrayList<>(getDefaults());
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(configPath)))) {
            return (List<SavedServer>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading server configurations: " + e.getMessage());
            return new ArrayList<>(getDefaults());
        }
    }

    /**
     * Saves a list of servers to the configuration file.
     */
    public static void saveAll(List<SavedServer> servers) {
        try {
            Path configDir = Path.of(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configPath = Path.of(CONFIG_DIR, CONFIG_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(configPath)))) {
                oos.writeObject(new ArrayList<>(servers));
            }
        } catch (Exception e) {
            System.err.println("Error saving server configurations: " + e.getMessage());
        }
    }

    /**
     * Returns default server configurations.
     */
    private static List<SavedServer> getDefaults() {
        List<SavedServer> defaults = new ArrayList<>();
        defaults.add(new SavedServer(
                "Local MLLP (2575)",
                ConnectionConfig.TransportMode.MLLP_TCP,
                "localhost",
                2575,
                "",
                false,
                10000
        ));
        defaults.add(new SavedServer(
                "Local HTTP (8080)",
                ConnectionConfig.TransportMode.HTTP,
                "",
                0,
                "http://localhost:8080/hl7",
                false,
                10000
        ));
        return defaults;
    }

    @Override
    public String toString() {
        return name;
    }
}
