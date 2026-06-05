package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record Configuration(int PORT, byte[] SECRET_KEY, byte[] PEPPER, String KEYSTORE_PASSWORD, String KEYSTORE_LOCATION) {
    private static final String TAG = "Configuration";
    public static final int DEFAULT_PORT = 6767;
    public static final String DEFAULT_SECRET_KEY = "testing123";
    public static final String DEFAULT_PEPPER = "pepper123";
    public static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";
    public static final String DEFAULT_KEYSTORE_LOCATION = "keystore.jks";

    public Configuration(int port, String secretKey, String pepper, String keystorePassword, String keystoreLocation) {
         this(port, secretKey.getBytes(StandardCharsets.UTF_8), pepper.getBytes(StandardCharsets.UTF_8), keystorePassword, keystoreLocation);
    }


    private static void ensureEnv(Map<String, String> env, String key, String defaultValue) {
        env.computeIfAbsent(key, k -> {
            Log.w(TAG, "Configuration value for " + k + " is missing, using default: " + defaultValue);
            return defaultValue;
        });
    }

    public static Configuration load() {
        var envPath = Path.of(".env");
        Map<String, String> env;
        try {
            env = Configuration.loadEnv(envPath);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load configuration from .env file, using defaults");
            return new Configuration(DEFAULT_PORT, DEFAULT_SECRET_KEY, DEFAULT_PEPPER, DEFAULT_KEYSTORE_PASSWORD, DEFAULT_KEYSTORE_LOCATION);
        }
        ensureEnv(env, "PORT", String.valueOf(DEFAULT_PORT));
        ensureEnv(env, "SECRET_KEY", DEFAULT_SECRET_KEY);
        ensureEnv(env, "PEPPER", DEFAULT_PEPPER);
        ensureEnv(env, "KEYSTORE_PASSWORD", DEFAULT_KEYSTORE_PASSWORD);
        ensureEnv(env, "KEYSTORE_LOCATION", DEFAULT_KEYSTORE_LOCATION);
        return new Configuration(
                Integer.parseInt(env.get("PORT")),
                env.get("SECRET_KEY"),
                env.get("PEPPER"),
                env.get("KEYSTORE_PASSWORD"),
                env.get("KEYSTORE_LOCATION")
        );
    }

    private static Map<String, String> loadEnv(Path path) throws IOException {
        Map<String, String> env = new HashMap<>();

        for (String line : Files.readAllLines(path)) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int equals = line.indexOf('=');
            if (equals == -1) {
                continue;
            }

            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();

            env.put(key, value);
        }

        return env;
    }
}
