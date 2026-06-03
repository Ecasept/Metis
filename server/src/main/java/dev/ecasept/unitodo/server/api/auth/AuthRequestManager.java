package dev.ecasept.unitodo.server.api.auth;

import dev.ecasept.unitodo.server.api.ApiResponse;
import dev.ecasept.unitodo.server.api.auth.models.UsernameAndPassword;
import dev.ecasept.unitodo.server.db.DBManager;
import dev.ecasept.unitodo.server.security.PasswordHasher;
import dev.ecasept.unitodo.server.security.SignedTokenManager;
import dev.ecasept.unitodo.server.serverlib.Response;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class AuthRequestManager {
    private final DBManager dbManager;

    public AuthRequestManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public Response<ApiResponse<String>> loginRequest(UsernameAndPassword req) {
        var user = dbManager.getUserByUsername(req.username());
        if (user.isPresent()) {
            boolean passwordValid;
            try {
                passwordValid = PasswordHasher.verifyPassword(req.password(), user.get().passwordHash());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException("Failed to verify password", e);
            } finally {
                req.password().shred();
            }
            if (passwordValid) {
                try {
                    var token = SignedTokenManager.generateToken(user.get().username());
                    return new Response<>(200, ApiResponse.success(token));
                } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new RuntimeException("Failed to generate token", e);
                }
            } else {
                return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
            }


        } else {
            req.password().shred();
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }
    }
    public Response<ApiResponse<String>> registerRequest(UsernameAndPassword req) {
        var user = dbManager.getUserByUsername(req.username());
        if (user.isPresent()) {
            return new Response<>(200, ApiResponse.error("Username already exists"));
        }

        String passwordHash;
        try {
            passwordHash = PasswordHasher.hashPassword(req.password());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
        req.password().shred();
        dbManager.createUser(req.username(), passwordHash);

        try {
            var token = SignedTokenManager.generateToken(req.username());
            return new Response<>(200, ApiResponse.success(token));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }
    public Response<ApiResponse<Void>> deleteAccountRequest(UsernameAndPassword req) {
        var user = dbManager.getUserByUsername(req.username());
        if (user.isEmpty()) {
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }

        boolean passwordValid;
        try {
            passwordValid = PasswordHasher.verifyPassword(req.password(), user.get().passwordHash());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to verify password", e);
        } finally {
            req.password().shred();
        }
        if (passwordValid) {
            dbManager.deleteUser(req.username());
            return new Response<>(200, ApiResponse.success(null));
        } else {
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }
    }
}
