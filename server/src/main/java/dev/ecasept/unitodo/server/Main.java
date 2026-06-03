package dev.ecasept.unitodo.server;

import dev.ecasept.unitodo.server.api.auth.models.UsernameAndPassword;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.server.api.ApiResponse;
import dev.ecasept.unitodo.server.api.auth.AuthRequestManager;
import dev.ecasept.unitodo.server.db.DBManager;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.server.serverlib.SimpleHttpsServer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class Main {
    public static void main(String[] args) throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        /*
        Routes:
        user auth:
        POST /api/auth/login
        POST /api/auth/register
        DELETE /api/auth/register

        sync:
        /api/sync/synchronize

         */

        DBManager dbManager = new DBManager();
        AuthRequestManager authRequestManager = new AuthRequestManager(dbManager);



        var server = new SimpleHttpsServer("password", "keystore.jks");
        server.addRoute("/", "GET", new StoreType<Void>() {}, new StoreType<RawData>() {}, (request) -> new Response<>(200, RawData.fromString("Hello, world!")));
        server.addRoute("/api/auth/login", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authRequestManager::loginRequest);
        server.addRoute("/api/auth/register", "POST", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<String>>() {}, authRequestManager::registerRequest);
        server.addRoute("/api/auth/register", "DELETE", new StoreType<UsernameAndPassword>() {}, new StoreType<ApiResponse<Void>>() {}, authRequestManager::deleteAccountRequest);
        server.run(Configuration.PORT);
    }
}
