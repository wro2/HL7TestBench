package com.hl7testbench.service;

import com.hl7testbench.model.ConnectionConfig;
import com.hl7testbench.model.ConnectionConfig.TransportMode;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Repository for persisting and loading server configurations.
 * Uses a simple properties-based format for human-readable storage.
 */
public class ServerConfigRepository {

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hl7testbench";
    private static final String CONFIG_FILE = "servers.json";
    private static final Path CONFIG_PATH = Path.of(CONFIG_DIR, CONFIG_FILE);

    /**
     * Loads all saved server configurations.
     */
    public List<SavedServerConfig> loadAll() {
        if (!Files.exists(CONFIG_PATH)) {
            return new ArrayList<>(getDefaults());
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            return parseJson(json);
        } catch (Exception e) {
            System.err.println("Error loading server configurations: " + e.getMessage());
            return new ArrayList<>(getDefaults());
        }
    }

    /**
     * Saves all server configurations.
     */
    public void saveAll(List<SavedServerConfig> servers) {
        try {
            Files.createDirectories(Path.of(CONFIG_DIR));
            String json = toJson(servers);
            Files.writeString(CONFIG_PATH, json);
        } catch (Exception e) {
            System.err.println("Error saving server configurations: " + e.getMessage());
        }
    }

    /**
     * Adds or updates a server configuration.
     */
    public void save(SavedServerConfig server) {
        List<SavedServerConfig> servers = loadAll();
        servers.removeIf(s -> s.name().equals(server.name()));
        servers.add(0, server);
        saveAll(servers);
    }

    /**
     * Deletes a server configuration by name.
     */
    public void delete(String name) {
        List<SavedServerConfig> servers = loadAll();
        servers.removeIf(s -> s.name().equals(name));
        saveAll(servers);
    }

    private List<SavedServerConfig> getDefaults() {
        List<SavedServerConfig> defaults = new ArrayList<>();
        defaults.add(new SavedServerConfig(
                "Local MLLP (2575)",
                TransportMode.MLLP_TCP,
                "localhost",
                2575,
                "",
                false,
                10000
        ));
        defaults.add(new SavedServerConfig(
                "Local HTTP (8080)",
                TransportMode.HTTP,
                "",
                0,
                "http://localhost:8080/hl7",
                false,
                10000
        ));
        return defaults;
    }

    /**
     * Simple JSON serialization without external dependencies.
     */
    private String toJson(List<SavedServerConfig> servers) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < servers.size(); i++) {
            SavedServerConfig s = servers.get(i);
            sb.append("  {\n");
            sb.append("    \"name\": ").append(jsonString(s.name())).append(",\n");
            sb.append("    \"mode\": ").append(jsonString(s.mode().name())).append(",\n");
            sb.append("    \"host\": ").append(jsonString(s.host())).append(",\n");
            sb.append("    \"port\": ").append(s.port()).append(",\n");
            sb.append("    \"httpUrl\": ").append(jsonString(s.httpUrl())).append(",\n");
            sb.append("    \"useTls\": ").append(s.useTls()).append(",\n");
            sb.append("    \"timeoutMs\": ").append(s.timeoutMs()).append("\n");
            sb.append("  }");
            if (i < servers.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /**
     * Simple JSON parsing without external dependencies.
     */
    private List<SavedServerConfig> parseJson(String json) {
        List<SavedServerConfig> servers = new ArrayList<>();

        String content = json.trim();
        if (!content.startsWith("[") || !content.endsWith("]")) {
            return getDefaults();
        }

        content = content.substring(1, content.length() - 1).trim();
        if (content.isEmpty()) {
            return servers;
        }

        int braceDepth = 0;
        int objectStart = -1;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (braceDepth == 0) objectStart = i;
                braceDepth++;
            } else if (c == '}') {
                braceDepth--;
                if (braceDepth == 0 && objectStart >= 0) {
                    String objectJson = content.substring(objectStart, i + 1);
                    SavedServerConfig server = parseServerObject(objectJson);
                    if (server != null) {
                        servers.add(server);
                    }
                    objectStart = -1;
                }
            }
        }

        return servers.isEmpty() ? getDefaults() : servers;
    }

    private SavedServerConfig parseServerObject(String json) {
        try {
            String name = extractJsonString(json, "name");
            String modeStr = extractJsonString(json, "mode");
            String host = extractJsonString(json, "host");
            int port = extractJsonInt(json, "port");
            String httpUrl = extractJsonString(json, "httpUrl");
            boolean useTls = extractJsonBoolean(json, "useTls");
            int timeoutMs = extractJsonInt(json, "timeoutMs");

            TransportMode mode = TransportMode.valueOf(modeStr);

            return new SavedServerConfig(name, mode, host, port, httpUrl, useTls, timeoutMs);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        int start = json.indexOf(pattern.replace("\\s*", ""));
        if (start < 0) {
            start = findKeyIndex(json, key);
            if (start < 0) return "";
        }

        int valueStart = json.indexOf(":", start) + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            return "";
        }

        valueStart++;
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == '"' && json.charAt(valueEnd - 1) != '\\') {
                break;
            }
            valueEnd++;
        }

        return json.substring(valueStart, valueEnd)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private int extractJsonInt(String json, String key) {
        int keyIndex = findKeyIndex(json, key);
        if (keyIndex < 0) return 0;

        int valueStart = json.indexOf(":", keyIndex) + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        int valueEnd = valueStart;
        while (valueEnd < json.length() && (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '-')) {
            valueEnd++;
        }

        try {
            return Integer.parseInt(json.substring(valueStart, valueEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean extractJsonBoolean(String json, String key) {
        int keyIndex = findKeyIndex(json, key);
        if (keyIndex < 0) return false;

        int valueStart = json.indexOf(":", keyIndex) + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        return json.substring(valueStart).trim().startsWith("true");
    }

    private int findKeyIndex(String json, String key) {
        return json.indexOf("\"" + key + "\"");
    }

    /**
     * Immutable saved server configuration record.
     */
    public record SavedServerConfig(
            String name,
            TransportMode mode,
            String host,
            int port,
            String httpUrl,
            boolean useTls,
            int timeoutMs
    ) {
        public ConnectionConfig toConnectionConfig() {
            return new ConnectionConfig(mode, host, port, httpUrl, useTls, null, null, timeoutMs);
        }

        public static SavedServerConfig fromConnectionConfig(String name, ConnectionConfig config) {
            return new SavedServerConfig(
                    name,
                    config.mode(),
                    config.host(),
                    config.port(),
                    config.httpUrl(),
                    config.useTls(),
                    config.timeoutMs()
            );
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
