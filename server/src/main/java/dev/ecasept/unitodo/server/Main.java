package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.server.api.SyncService;
import dev.ecasept.unitodo.server.db.ServerDatabaseRepository;
import dev.ecasept.unitodo.server.security.PasswordHasherService;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.server.serverlib.RouteHandler;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.api.*;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.server.api.AuthService;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.server.serverlib.SimpleServer;
import dev.ecasept.unitodo.shared.sync.Synchronizer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;

public class Main {
    private static final String TAG = "Main";
    public static void main(String[] args) {
        var config = Configuration.load();
        var tokenService = new SignedTokenService();
        var passwordHasherService = new PasswordHasherService(config);
        var classLoader = Main.class.getClassLoader();
        DatabaseController databaseController;
        try {
            databaseController = new DatabaseController(classLoader, config.DB_URL());
        } catch (DatabaseException e) {
            Log.e(TAG, "Failed to initialize database controller", e);
            return;
        }
        try {
            var queryBuilder = new QueryBuilder(databaseController);
            var databaseRepository = new ServerDatabaseRepository(queryBuilder);
            var authService = new AuthService(databaseRepository, passwordHasherService, tokenService, config);
            var synchronizer = new Synchronizer();
            var syncService = new SyncService(databaseRepository, synchronizer, tokenService, config);

            var server = new SimpleServer("changeit", "keystore.jks", config.USE_HTTPS());
            server.addRoute("/", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, (r, headers) -> new Response<>(200, RawData.fromString("Hello, world!")));
            server.addRoute("/api/auth/login", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::loginRequest);
            server.addRoute("/api/auth/register", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::registerRequest);
            server.addRoute("/api/users/delete", "POST", new StoreType<Password>() {}, new StoreType<ApiResponse<Void>>() {}, authService::deleteAccountRequest);
            server.addRoute("/api/data/sync", "POST", new StoreType<SyncRequest>() {}, new StoreType<ApiResponse<SyncResponse>>() {}, syncService::syncRequest);

            server.addRoute("/about", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, serveStaticHTMLFile("about.html"));
            server.addRoute("/build", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, serveStaticHTMLFile("build.html"));

            server.run(config.PORT());
        } catch (Exception e) {
            try {
                databaseController.close();
            } catch (DatabaseException ex) {
                e.addSuppressed(ex);
            }
            Log.e(TAG, "An error occurred while running the server", e);
        }
    }

    private static RouteHandler<Void, RawData> serveStaticHTMLFile(String filePath) {
        return (request, headers) -> {
            try (var file = Main.class.getClassLoader().getResourceAsStream(filePath)) {
                if (file == null) {
                    Log.e(TAG, filePath + " not found");
                    return new Response<>(404, RawData.fromString("Not Found"));
                }
                var content = new String(file.readAllBytes());
                var res = new Response<>(200, RawData.fromString(content));
                res.headers().put("Content-Type", "text/html; charset=UTF-8");
                return res;
            } catch (IOException e) {
                Log.e(TAG, "Failed to read " + filePath, e);
                return new Response<>(500, RawData.fromString("Internal Server Error"));
            }
        };
    }
}
