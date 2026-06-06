package dev.ecasept.unitodo.server.api.auth;

import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.UsernameAndPassword;
import dev.ecasept.unitodo.server.db.DatabaseRepository;
import dev.ecasept.unitodo.server.security.PasswordHasherService;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.server.serverlib.Response;

public class AuthService {
    private final DatabaseRepository databaseRepository;
    private final PasswordHasherService passwordHasherService;
    private final SignedTokenService tokenService;
    private final Configuration config;

    public AuthService(DatabaseRepository databaseRepository, PasswordHasherService passwordHasherService, SignedTokenService tokenService, Configuration config) {
        this.databaseRepository = databaseRepository;
        this.passwordHasherService = passwordHasherService;
        this.tokenService = tokenService;
        this.config = config;
    }

    public Response<ApiResponse<String>> loginRequest(UsernameAndPassword req) {
        if (req.username().isEmpty() || req.password().pw().length == 0) {
            req.password().shred();
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }
        var user = databaseRepository.getUserByUsername(req.username());
        if (user.isPresent()) {
            boolean passwordValid;
            try {
                passwordValid = passwordHasherService.verifyPassword(req.password(), user.get().passwordHash());
            } finally {
                req.password().shred();
            }
            if (passwordValid) {
                var token = tokenService.generateToken(user.get().username(), config.SECRET_KEY());
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
        if (req.username().isEmpty() || req.password().pw().length == 0) {
            req.password().shred();
            return new Response<>(200, ApiResponse.error("Username and password cannot be empty"));
        }
        var user = databaseRepository.getUserByUsername(req.username());
        if (user.isPresent()) {
            req.password().shred();
            return new Response<>(200, ApiResponse.error("Username already exists"));
        }

        String passwordHash;
        passwordHash = passwordHasherService.hashPassword(req.password());
        req.password().shred();
        databaseRepository.createUser(req.username(), passwordHash);

        var token = tokenService.generateToken(req.username(), config.SECRET_KEY());
        return new Response<>(200, ApiResponse.success(token));
    }
    public Response<ApiResponse<Void>> deleteAccountRequest(UsernameAndPassword req) {
        var user = databaseRepository.getUserByUsername(req.username());
        if (user.isEmpty()) {
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }

        boolean passwordValid;
        try {
            passwordValid = passwordHasherService.verifyPassword(req.password(), user.get().passwordHash());
        } finally {
            req.password().shred();
        }
        if (passwordValid) {
            databaseRepository.deleteUser(req.username());
            return new Response<>(200, ApiResponse.success(null));
        } else {
            return new Response<>(200, ApiResponse.error("Invalid username or credentials"));
        }
    }
}
