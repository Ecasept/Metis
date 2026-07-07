package dev.ecasept.unitodo.server.api;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.server.db.ServerDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ErrorCode;
import dev.ecasept.unitodo.shared.models.api.Password;
import dev.ecasept.unitodo.shared.models.api.UsernameAndPassword;
import dev.ecasept.unitodo.server.security.PasswordHasherService;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.server.serverlib.Response;


public class AuthService {
    private final ServerDatabaseRepository db;
    private final PasswordHasherService passwordHasherService;
    private final SignedTokenService tokenService;
    private final Configuration config;

    public AuthService(ServerDatabaseRepository db, PasswordHasherService passwordHasherService, SignedTokenService tokenService, Configuration config) {
        this.db = db;
        this.passwordHasherService = passwordHasherService;
        this.tokenService = tokenService;
        this.config = config;
    }

    public Response<ApiResponse<String>> loginRequest(UsernameAndPassword req, Headers headers) throws DatabaseException {
        try (var password = req.password()) {
            if (req.username().isEmpty() || password.pw().length == 0) {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid username or credentials", ErrorCode.AUTH_INVALID_CREDENTIALS));
            }
            var user = db.getUserByUsername(req.username());
            if (user.isEmpty()) {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid username or credentials", ErrorCode.AUTH_INVALID_CREDENTIALS));
            }
            boolean passwordValid = passwordHasherService.verifyPassword(password, user.get().passwordHash());
            if (passwordValid) {
                var token = tokenService.generateToken(user.get().userId().toString(), config.SECRET_KEY());
                return new Response<>(200, ApiResponse.success(token));
            } else {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid username or credentials", ErrorCode.AUTH_INVALID_CREDENTIALS));
            }
        }
    }
    public Response<ApiResponse<String>> registerRequest(UsernameAndPassword req, Headers headers) throws DatabaseException {
        try (var password = req.password()) {
            if (req.username().isEmpty() || password.pw().length == 0) {
                return new Response<>(400, ApiResponse.error("Bad Request: Username and password cannot be empty", ErrorCode.AUTH_USERNAME_TAKEN));
            }
            var user = db.getUserByUsername(req.username());
            if (user.isPresent()) {
                return new Response<>(409, ApiResponse.error("Username already exists", ErrorCode.AUTH_USERNAME_TAKEN));
            }

            String passwordHash;
            passwordHash = passwordHasherService.hashPassword(password);
            var userId = db.createUser(req.username(), passwordHash);

            var token = tokenService.generateToken(userId.toString(), config.SECRET_KEY());
            return new Response<>(200, ApiResponse.success(token));
        }
    }
    public Response<ApiResponse<Void>> deleteAccountRequest(Password pw, Headers headers) throws DatabaseException {
        try (pw) {
            var userIdOptional = Auth.verifyAuth(headers, tokenService, config.SECRET_KEY());
            if (userIdOptional.isEmpty()) {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid session token", ErrorCode.AUTH_TOKEN_INVALID));
            }
            var userId = userIdOptional.get();

            var user = db.getUser(userId);
            if (user.isEmpty()) {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid session token", ErrorCode.AUTH_TOKEN_INVALID));
            }

            boolean passwordValid = passwordHasherService.verifyPassword(pw, user.get().passwordHash());
            if (passwordValid) {
                db.deleteUser(userId);
                return new Response<>(204, ApiResponse.success(null));
            } else {
                return new Response<>(401, ApiResponse.error("Unauthorized: Invalid username or credentials", ErrorCode.AUTH_INVALID_CREDENTIALS));
            }
        }
    }
}
