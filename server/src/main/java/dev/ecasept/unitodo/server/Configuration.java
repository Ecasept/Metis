package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;

/** Stores the configuration for the server */
public record Configuration(int PORT, byte[] SECRET_KEY, byte[] PEPPER, String KEYSTORE_PASSWORD, String KEYSTORE_LOCATION, String DB_URL, TemporalAmount TOMBSTONE_TTL, boolean USE_HTTPS) {
    private static final String TAG = "Configuration";
    private static final int DEFAULT_PORT = 6767;
    private static final String DEFAULT_SECRET_KEY = "testing123";
    private static final String DEFAULT_PEPPER = "pepper123";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";
    private static final String DEFAULT_KEYSTORE_LOCATION = "keystore.jks";
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:unitodo.db";
    private static final TemporalAmount DEFAULT_TOMBSTONE_TTL = Period.ofMonths(1);
    private static final boolean DEFAULT_USE_HTTPS = true;

    private Configuration(int port, String secretKey, String pepper, String keystorePassword, String keystoreLocation, String dbUrl, TemporalAmount tombstoneTtl, boolean useHttps) {
         this(port, secretKey.getBytes(StandardCharsets.UTF_8), pepper.getBytes(StandardCharsets.UTF_8), keystorePassword, keystoreLocation, dbUrl, tombstoneTtl, useHttps);
    }


    private static void ensureEnv(Map<String, String> env, String key, String defaultValue) {
        env.computeIfAbsent(key, k -> {
            Log.w(TAG, "Configuration value for " + k + " is missing, using default: " + defaultValue);
            return defaultValue;
        });
    }

    /** Loads the configuration from the .env file, or uses defaults if the file is missing or invalid, and returns it */
    public static Configuration load() {
        var envPath = Path.of(".env");
        Map<String, String> env;
        try {
            env = Configuration.loadEnv(envPath);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load configuration from .env file, using defaults", e);
            return new Configuration(DEFAULT_PORT, DEFAULT_SECRET_KEY, DEFAULT_PEPPER, DEFAULT_KEYSTORE_PASSWORD, DEFAULT_KEYSTORE_LOCATION, DEFAULT_DB_URL, DEFAULT_TOMBSTONE_TTL, DEFAULT_USE_HTTPS);
        }
        ensureEnv(env, "PORT", String.valueOf(DEFAULT_PORT));
        ensureEnv(env, "SECRET_KEY", DEFAULT_SECRET_KEY);
        ensureEnv(env, "PEPPER", DEFAULT_PEPPER);
        ensureEnv(env, "KEYSTORE_PASSWORD", DEFAULT_KEYSTORE_PASSWORD);
        ensureEnv(env, "KEYSTORE_LOCATION", DEFAULT_KEYSTORE_LOCATION);
        ensureEnv(env, "DB_URL", DEFAULT_DB_URL);
        ensureEnv(env, "TOMBSTONE_TTL", String.valueOf(DEFAULT_TOMBSTONE_TTL.get(ChronoUnit.DAYS)));
        ensureEnv(env, "USE_HTTPS", String.valueOf(DEFAULT_USE_HTTPS));
        return new Configuration(
                Integer.parseInt(env.get("PORT")),
                env.get("SECRET_KEY"),
                env.get("PEPPER"),
                env.get("KEYSTORE_PASSWORD"),
                env.get("KEYSTORE_LOCATION"),
                env.get("DB_URL"),
                Period.ofDays(Integer.parseInt(env.get("TOMBSTONE_TTL"))),
                Boolean.parseBoolean(env.get("USE_HTTPS"))
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
