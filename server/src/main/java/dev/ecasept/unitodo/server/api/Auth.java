package dev.ecasept.unitodo.server.api;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.server.security.SessionToken;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.shared.models.api.ErrorCode;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import java.time.Clock;
import java.util.UUID;

public class Auth {
    private final SignedTokenService tokenService;
    private final Configuration config;
    private final Clock clock;

    public Auth(SignedTokenService tokenService, Configuration config, Clock clock) {
        this.tokenService = tokenService;
        this.config = config;
        this.clock = clock;
    }

    public record Result(UUID userId, ErrorCode errorCode) {
        public boolean isValid() { return errorCode == null; }
    }

    public String issueToken(UUID userId) {
        var claims = new SessionToken(userId, clock.instant().plus(config.SESSION_TTL()).getEpochSecond());
        return tokenService.generateToken(Serializer.createDefault().serialize(claims, new StoreType<>() {}), config.SECRET_KEY());
    }

    public Result verifyAuth(Headers headers) {
        var header = headers.getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return new Result(null, ErrorCode.AUTH_TOKEN_INVALID);
        }
        var payload = tokenService.verifyAndGetPayload(header.substring(7), config.SECRET_KEY());
        if (payload.isEmpty()) {
            return new Result(null, ErrorCode.AUTH_TOKEN_INVALID);
        }
        try {
            var claims = Serializer.createDefault().deserialize(payload.get(), new StoreType<SessionToken>() {});
            if (clock.instant().getEpochSecond() >= claims.expiresAt()) {
                return new Result(null, ErrorCode.AUTH_TOKEN_EXPIRED);
            }
            return new Result(claims.userId(), null);
        } catch (SerializationException e) {
            return new Result(null, ErrorCode.AUTH_TOKEN_INVALID);
        }
    }
}
