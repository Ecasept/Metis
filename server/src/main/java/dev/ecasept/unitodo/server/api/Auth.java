package dev.ecasept.unitodo.server.api;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.server.security.SignedTokenService;

import java.util.Optional;
import java.util.UUID;

public class Auth {
    /** Verifies that the request headers have an associated valid session token.
     *
     * @param headers The headers of the request
     * @return The uuid of the user, if he is authenticated, or nothing if no session token is present, or it is invalid
     */
    public static Optional<UUID> verifyAuth(Headers headers, SignedTokenService tokenService, byte[] secretKey) {
        var authHeader = headers.getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var token = authHeader.substring(7);
            var sessionToken = tokenService.verifyAndGetPayload(token, secretKey);
            if (sessionToken.isPresent()) {
                try {
                    var userId = UUID.fromString(sessionToken.get());
                    return Optional.of(userId);
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}