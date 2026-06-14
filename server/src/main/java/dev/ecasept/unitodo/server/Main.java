package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.server.api.ServerSynchronizer;
import dev.ecasept.unitodo.server.api.SyncService;
import dev.ecasept.unitodo.server.db.ServerDatabaseRepository;
import dev.ecasept.unitodo.server.security.PasswordHasherService;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.api.*;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.server.api.AuthService;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.server.serverlib.SimpleHttpsServer;
import dev.ecasept.unitodo.shared.utils.Log;

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
            var synchronizer = new ServerSynchronizer();
            var syncService = new SyncService(databaseRepository, synchronizer, tokenService, config);

            var server = new SimpleHttpsServer("changeit", "keystore.jks");
            server.addRoute("/", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, (r, headers) -> new Response<>(200, RawData.fromString("Hello, world!")));
            server.addRoute("/api/auth/login", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::loginRequest);
            server.addRoute("/api/auth/register", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authService::registerRequest);
            server.addRoute("/api/users/delete", "POST", new StoreType<Password>() {}, new StoreType<ApiResponse<Void>>() {}, authService::deleteAccountRequest);
            server.addRoute("/api/sync/synchronize", "POST", new StoreType<SyncRequest>() {}, new StoreType<ApiResponse<SyncResponse>>() {}, syncService::syncRequest);
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
}
