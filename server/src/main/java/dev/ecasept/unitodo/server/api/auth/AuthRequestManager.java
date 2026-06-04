package dev.ecasept.unitodo.server.api.auth;

import dev.ecasept.unitodo.server.Configuration;
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
            } finally {
                req.password().shred();
            }
            if (passwordValid) {
                var token = SignedTokenManager.generateToken(user.get().username(), Configuration.SECRET_KEY);
                return new Response<>(200, ApiResponse.success(token));
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
        passwordHash = PasswordHasher.hashPassword(req.password());
        req.password().shred();
        dbManager.createUser(req.username(), passwordHash);

        var token = SignedTokenManager.generateToken(req.username(), Configuration.SECRET_KEY);
        return new Response<>(200, ApiResponse.success(token));
    }
    public Response<ApiResponse<Void>> deleteAccountRequest(UsernameAndPassword req) {
        var user = dbManager.getUserByUsername(req.username());
        if (user.isEmpty()) {
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }

        boolean passwordValid;
        try {
            passwordValid = PasswordHasher.verifyPassword(req.password(), user.get().passwordHash());
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
