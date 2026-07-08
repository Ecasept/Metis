package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.serialization.annotations.SerialInstance;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

/** Error codes that the server can return over the API so the client can show relevant error messages */
@Serializable
public enum ErrorCode {
    @SerialInstance(tag=1)
    AUTH_INVALID_CREDENTIALS ("Benutzername oder Passwort ungültig"),
    @SerialInstance(tag=2)
    AUTH_EMPTY_FIELDS ("Benutzername oder Passwort leer"),
    @SerialInstance(tag=3)
    AUTH_USERNAME_TAKEN ("Benutzername bereits vergeben"),
    @SerialInstance(tag=4)
    AUTH_TOKEN_INVALID ("Ungültiger Token. Versuche dich erneut anzumelden"),
    @SerialInstance(tag=5)
    UNKNOWN ("Unbekannter Fehler");

    private final String message;
    ErrorCode(String msg) {
        this.message = msg;
    }

    /** Returns the user-facing error message for this error code */
    public String getMessage() {
        return message;
    }
}
