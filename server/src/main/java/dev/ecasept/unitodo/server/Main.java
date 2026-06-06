package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.server.security.PasswordHasherService;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.shared.models.api.UsernameAndPassword;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.server.api.auth.AuthService;
import dev.ecasept.unitodo.server.db.DatabaseRepository;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.server.serverlib.SimpleHttpsServer;

public class Main {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));

        /*
        Routes:
        user auth:
        POST /api/auth/login
        POST /api/auth/register
        DELETE /api/auth/register

        sync:
        /api/sync/synchronize

         */

        Configuration config = Configuration.load();
        SignedTokenService tokenService = new SignedTokenService();
        PasswordHasherService passwordHasherService = new PasswordHasherService(config);
        DatabaseRepository databaseRepository = new DatabaseRepository();
        AuthService authService = new AuthService(databaseRepository, passwordHasherService, tokenService, config);

        var server = new SimpleHttpsServer("changeit", "keystore.jks");
        server.addRoute("/", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, (request) -> new Response<>(200, RawData.fromString("Hello, world!")));
        server.addRoute("/api/auth/login", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::loginRequest);
        server.addRoute("/api/auth/register", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::registerRequest);
        server.addRoute("/api/auth/register", "DELETE", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<Void>>() {}, authService::deleteAccountRequest);
        server.run(config.PORT());
    }
}
