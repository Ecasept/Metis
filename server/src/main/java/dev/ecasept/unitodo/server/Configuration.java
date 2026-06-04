package dev.ecasept.unitodo.server;

import java.nio.charset.StandardCharsets;

public final class Configuration {
    public static final int PORT = 6767;
    public static final byte[] SECRET_KEY = "testing123".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PEPPER = "pepper123".getBytes(StandardCharsets.UTF_8);
}
